/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX.CheckOrphanedCallback;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.impl.SVNAuthenticationManager;
import org.omegat.util.Language;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TMXWriter2;
import org.omegat.util.TestPreferencesInitializer;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * This is test for team project concurrent modification. It doesn't simple junit test, but looks like
 * 'integration' test.
 * 
 * This test prepare scenario, execute separate JVMs for concurrent updates, then check remote repository
 * data.
 * 
 * Each child process updates own segments with source1..5/0/1/2/3 by values from 1 and more. Segment source/0
 * updated each time, but source/1/2/3 updated once per cycle. After process will be finished, values in tmx
 * should be in right order, i.e. only by increasing order. That means user will not commit previous
 * translation for other user's segments.
 * 
 * Segment with 'concurrent' source will be modified by all users by values from 1 and more with user's
 * prefix. Conflicts should be resolved by choose higher value. After process will be finished, values in
 * 'concurrent' segment should be also increased only.
 * 
 * Each child saves Integer.MAXVALUE as last translation, but current OmegaT implementation doesn't require to
 * commit it, see "GIT_CONFLICT=Push failed. Will be synchronized next time."
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * 
 *         TODO: "svn: E160028: Commit failed" during commit
 */
public class TestTeamIntegration {
    static final String DIR = "/tmp/teamtest";
    static final String REPO = System.getProperty("omegat.test.repo", "git@github.com:alex73/trans.git");
    // static final String REPO = "svn+ssh://alex73@svn.code.sf.net/p/mappy/test/";
    // static final String REPO = "https://github.com/alex73/trans/trunk/";
    static int PROCESS_SECONDS = 4 * 60 * 60;
    static {
        Optional.ofNullable(System.getProperty("omegat.test.duration"))
                .ifPresent(duration -> PROCESS_SECONDS = Integer.parseInt(duration));
    }
    static int MAX_DELAY_SECONDS = 15;
    static int SEG_COUNT = 4;

    static Language SRC_LANG = new Language("en");
    static Language TRG_LANG = new Language("be");

    static final String[] THREADS = new String[] { "s1", "s2", "s3" };

    static Team repo;

    public static void main(String[] args) throws Exception {
        String startVersion = prepareRepo();

        Run[] runs = new Run[THREADS.length];
        for (int i = 0; i < THREADS.length; i++) {
            runs[i] = new Run(THREADS[i], new File(DIR, THREADS[i]), MAX_DELAY_SECONDS);
        }
        for (int i = 0; i < THREADS.length; i++) {
            runs[i].start();
        }
        boolean alive;
        do {
            alive = false;
            for (int i = 0; i < THREADS.length; i++) {
                if (runs[i].finished) {
                    if (runs[i].result != 200) {
                        for (Run r : runs) {
                            r.end();
                        }
                        System.err.println("==================== EXIT BY ERROR ====================");
                        System.exit(1);
                    }
                } else {
                    alive = true;
                }
            }
            Thread.sleep(500);
        } while (alive);

        repo = createRepo2(REPO, new File(DIR, "repo"));
        repo.update();

        System.err.println("Check repo");

        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        checkRepo(startVersion);

        System.err.println("Processed successfully");
    }

    /**
     * Check repository after children processed.
     */
    static void checkRepo(String startVersion) throws Exception {
        List<String> segments = new ArrayList<String>();
        for (String th : THREADS) {
            for (int c = 0; c < SEG_COUNT; c++) {
                segments.add(th + "/" + c);
            }
        }

        Map<String, List<Long>> data = new TreeMap<String, List<Long>>();
        for (String th : segments) {
            data.put(th, new ArrayList<Long>());
            data.get(th).add(0L);
        }
        data.put(TestTeamIntegrationChild.CONCURRENT_NAME, new ArrayList<Long>());
        data.get(TestTeamIntegrationChild.CONCURRENT_NAME).add(0L);

        ProjectTMX tmx = null;
        int tmxCount = 0;
        for (String rev : repo.listRevisions(startVersion)) {
            repo.checkout(rev);
            tmx = new ProjectTMX(SRC_LANG, TRG_LANG, false, new File(repo.getDir(), "omegat/project_save.tmx"),
                    checkOrphanedCallback);

            for (String th : data.keySet()) {
                TMXEntry en = tmx.getDefaultTranslation(th);
                long value = en == null ? 0 : Long.parseLong(en.translation);
                data.get(th).add(value);
            }
            tmxCount++;
        }

        System.err.println("Values :");
        for (String th : segments) {
            out(th);
        }
        System.err.println();
        for (int i = 0; i < tmxCount; i++) {
            for (String th : segments) {
                out(data.get(th).get(i));
            }
            System.err.println();
        }
        boolean ok = true;
        for (String th : data.keySet()) {
            long prev = 0;
            for (long v : data.get(th)) {
                if (v < prev) {
                    System.err.println("Wrong order in " + th);
                    ok = false;
                    break;
                } else {
                    prev = v;
                }
            }
        }
        if (ok) {
            System.err.println("All commits look good");
        }
    }

    static void out(Object v) {
        String s = v.toString();
        System.err.print("                  ".substring(0, 14 - s.length()) + s + " ");
    }

    static String merge(List<String> cp, char separator) {
        String o = "";
        for (String c : cp) {
            o += separator;
            o += c;
        }
        return o.substring(1);
    }

    /**
     * Prepare repository.
     */
    static String prepareRepo() throws Exception {
        File tmp = new File(DIR);
        FileUtils.deleteDirectory(tmp);
        if (tmp.exists()) {
            throw new Exception("Impossible to delete test dir");
        }
        tmp.mkdirs();

        File origDir = new File(tmp, "repo");
        origDir.mkdir();

        ProjectProperties config = createConfig(REPO, origDir);

        RemoteRepositoryProvider remote = new RemoteRepositoryProvider(config.getProjectRootDir(), config.getRepositories());
        remote.switchAllToLatest();

        new File(origDir, "omegat").mkdirs();
        File f = new File(origDir, "omegat/project_save.tmx");
        TMXWriter2 wr = new TMXWriter2(f, SRC_LANG, TRG_LANG, true, false, true);
        wr.close();

        ProjectFileStorage.writeProjectFile(config);

        remote.copyFilesFromProjectToRepo("omegat.project", null);
        remote.commitFiles("omegat.project", "Prepare for team test");
        remote.copyFilesFromProjectToRepo("omegat/project_save.tmx", null);
        remote.commitFiles("omegat/project_save.tmx", "Prepare for team test");

        return remote.getVersion("omegat/project_save.tmx");
    }

    static ProjectProperties createConfig(String repoUrl, File dir) throws Exception {
        ProjectProperties config = new ProjectProperties(dir);
        config.setSourceLanguage(SRC_LANG);
        config.setTargetLanguage(TRG_LANG);
        RepositoryDefinition def = new RepositoryDefinition();
        if (repoUrl.startsWith("git") || repoUrl.endsWith(".git")) {
            def.setType("git");
        } else if (repoUrl.startsWith("svn") || repoUrl.startsWith("http") || repoUrl.endsWith(".svn")) {
            def.setType("svn");
        } else {
            throw new RuntimeException("Unknown repo");
        }
        def.setUrl(repoUrl);
        RepositoryMapping m = new RepositoryMapping();
        m.setLocal("");
        m.setRepository("");
        def.getMapping().add(m);
        config.setRepositories(new ArrayList<RepositoryDefinition>());
        config.getRepositories().add(def);
        return config;
    }

    static Team createRepo2(String url, File dir) throws Exception {
        File repoDir = Stream.of(new File(dir, RemoteRepositoryProvider.REPO_SUBDIR).listFiles())
                .filter(File::isDirectory).findFirst().get();
        if (url.startsWith("git") || url.endsWith(".git")) {
            return new GitTeam(repoDir);
        } else if (url.startsWith("svn") || url.startsWith("http") || url.endsWith(".svn")) {
            return new SvnTeam(repoDir, url);
        } else {
            throw new Exception("Unknown repo");
        }
    }

    /**
     * Child process handling.
     */
    static class Run extends Thread {
        volatile Process p;
        volatile int result;
        volatile boolean finished;
        String source;

        public Run(String source, File dir, int delay) throws Exception {
            this.source = source;
            URLClassLoader cl = (URLClassLoader) TestTeamIntegration.class.getClassLoader();
            List<String> cp = new ArrayList<String>();
            for (URL u : cl.getURLs()) {
                cp.add(u.getFile());
            }
            FileUtils.copyFile(new File(DIR + "/repo/omegat.project"), new File(DIR + "/" + source
                    + "/omegat.project"));
            new File(DIR + "/" + source + "/omegat/").mkdirs();

            System.err.println("Execute: " + source + " " + (PROCESS_SECONDS * 1000) + " "
                    + dir.getAbsolutePath() + " " + REPO + " " + delay + " " + SEG_COUNT);
            ProcessBuilder pb = new ProcessBuilder("java", "-Duser.name=" + source, "-cp", merge(cp,
                    File.pathSeparatorChar), TestTeamIntegrationChild.class.getName(), source,
                    Long.toString(PROCESS_SECONDS * 1000), dir.getAbsolutePath(), REPO,
                    Integer.toString(delay), Integer.toString(SEG_COUNT));
            pb.inheritIO();
            p = pb.start();
        }

        @Override
        public void run() {
            try {
                result = p.waitFor();
            } catch (Exception ex) {
                result = -1;
            }
            if (result != 200) {
                System.err.println("Error result from " + source);
            } else {
                System.err.println("==================== " + source + " finished OK ====================");
            }
            finished = true;
        }

        public void end() {
            if (p.isAlive()) {
                p.destroyForcibly();
            }
        }
    }

    static CheckOrphanedCallback checkOrphanedCallback = new CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return true;
        }

        public boolean existEntryInProject(EntryKey key) {
            return true;
        }
    };

    interface Team {
        List<String> listRevisions(String from) throws Exception;

        void checkout(String rev) throws Exception;

        void update() throws Exception;

        File getDir();
    }

    public static class GitTeam implements Team {
        final Repository repository;
        final File dir;

        public GitTeam(File dir) throws Exception {
            this.dir = dir;
            repository = Git.open(dir).getRepository();
        }

        public List<String> listRevisions(String from) throws Exception {
            try (Git git = new Git(repository)) {
                LogCommand cmd = git.log();
                List<String> result = new ArrayList<String>();
                for (RevCommit commit : cmd.call()) {
                    if (commit.getName().equals(from)) {
                        break;
                    }
                    result.add(commit.getName());
                }
                Collections.reverse(result);
                return result;
            }
        }

        public void update() throws Exception {
            try (Git git = new Git(repository)) {
                git.fetch().call();
                git.checkout().setName("origin/master").call();
            }
        }

        public void checkout(String rev) throws Exception {
            try (Git git = new Git(repository)) {
                git.checkout().setName(rev).call();
            }
        }

        public File getDir() {
            return dir;
        }
    }

    public static class SvnTeam implements Team {
        SVNClientManager ourClientManager;
        File dir;

        public SvnTeam(File dir, String url) throws Exception {
            this.dir = dir;
            ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
            ISVNAuthenticationManager authManager = new SVNAuthenticationManager(url,
                    SVNURL.parseURIEncoded(url).getUserInfo(), null, null);
            ourClientManager = SVNClientManager.newInstance(options, authManager);
        }

        public List<String> listRevisions(String from) throws Exception {
            final List<String> result = new ArrayList<String>();
            ourClientManager.getLogClient().doLog(
                    new File[] { new File(repo.getDir(), "omegat/project_save.tmx") },
                    SVNRevision.create(Long.parseLong(from)), SVNRevision.HEAD, false, false,
                    Integer.MAX_VALUE, new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry en) throws SVNException {
                            result.add("" + en.getRevision());
                        }
                    });
            return result;
        }

        public void update() throws Exception {
            ourClientManager.getUpdateClient().doUpdate(dir, SVNRevision.HEAD, SVNDepth.INFINITY, false,
                    false);
        }

        public void checkout(String rev) throws Exception {
            ourClientManager.getUpdateClient().doUpdate(dir, SVNRevision.create(Long.parseLong(rev)),
                    SVNDepth.INFINITY, false, false);
        }

        public File getDir() {
            return dir;
        }
    }
}
