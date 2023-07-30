/*
 * The MIT License
 *
 * Copyright 2020 Loghi Perinpanayagam.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.ml.e2e;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Functions;
import hudson.model.Computer;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.remoting.VirtualChannel;
import hudson.slaves.CommandLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.util.FormValidation;
import io.jenkins.plugins.ml.IPythonBuilder;
import io.jenkins.plugins.ml.IPythonServerGlobalConfigurationTest;
import org.jenkinsci.plugins.gitclient.JGitTool;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NotebookTest {

    private HtmlForm form;
    private HtmlPage configPage;
    private FreeStyleProject project;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule() {
        private void purgeSlaves() {
            List<Computer> disconnectingComputers = new ArrayList<Computer>();
            List<VirtualChannel> closingChannels = new ArrayList<VirtualChannel>();
            for (Computer computer : jenkins.getComputers()) {
                if (!(computer instanceof SlaveComputer)) {
                    continue;
                }
                // disconnect slaves.
                // retrieve the channel before disconnecting.
                // even a computer gets offline, channel delays to close.
                if (!computer.isOffline()) {
                    VirtualChannel ch = computer.getChannel();
                    computer.disconnect(null);
                    disconnectingComputers.add(computer);
                    closingChannels.add(ch);
                }
            }

            try {
                // Wait for all computers disconnected and all channels closed.
                for (Computer computer : disconnectingComputers) {
                    computer.waitUntilOffline();
                }
                for (VirtualChannel ch : closingChannels) {
                    ch.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @After
        public void tearDown() {
            purgeSlaves();
        }
    };

    @Before
    public void createMocks() throws Exception {
        configPage = jenkins.createWebClient().goTo("configure");
        project = jenkins.createFreeStyleProject();
        form = configPage.getFormByName("config");
        IPythonServerGlobalConfigurationTest.getButton(form, "Add new Kernel")
                .click();
        List<HtmlInput> serverName = form.getInputsByName("_.serverName");
        serverName.get(0).setValueAttribute("pyServer");
        List<HtmlInput> kernel = form.getInputsByName("_.kernel");
        kernel.get(0).setValueAttribute("python");
        List<HtmlInput> launchTimeout = form.getInputsByName("_.launchTimeout");
        launchTimeout.get(0).setValueAttribute("5");
        List<HtmlInput> maxResults = form.getInputsByName("_.maxResults");
        maxResults.get(0).setValueAttribute("3");
        // submit the global configurations
        jenkins.submit(form);
        // create an agent using the docker command
        DumbSlave s;
        if (!Functions.isWindows()) {
            s = new DumbSlave("s", "/home/jenkins", new CommandLauncher("docker run -i --rm --init loghijiaha/ml-agent java -jar /usr/share/jenkins/agent.jar"));
            jenkins.jenkins.addNode(s);
            project.setAssignedNode(s);
        }
        // TODO for windows : after creating a windows supported docker image

    }
    @Test
    public void testAdditionBuild() throws Exception {

        Assume.assumeTrue(!Functions.isWindows());

        IPythonBuilder builder = new IPythonBuilder("32+6", " ", "text", "test", "python");
        project.getBuildersList().add(builder);

        project.save();

        QueueTaskFuture<FreeStyleBuild> taskFuture = project.scheduleBuild2(0);
        FreeStyleBuild freeStyleBuild = taskFuture.get();
        jenkins.waitForCompletion(freeStyleBuild);
        jenkins.assertBuildStatusSuccess(freeStyleBuild);
        jenkins.assertLogContains("38", freeStyleBuild);

    }

    @Test
    public void testJobConfigReload() throws Exception {
        String PROJECT_NAME = "demo";
        project = jenkins.createFreeStyleProject(PROJECT_NAME);
        // created a builder and added
        IPythonBuilder builder = new IPythonBuilder("", "train.py", "file", "test", "python");
        project.getBuildersList().add(builder);

        // configure web client
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        HtmlPage jobConfigPage = webClient.getPage(project, "configure");
        form = jobConfigPage.getFormByName("config");
        configPage.refresh();
        // check whether the configuration is persisted or not
        List<HtmlInput> task = form.getInputsByName("task");
        List<HtmlInput> filePath = form.getInputsByName("filePath");
        assertEquals("train.py", filePath.get(0).getValueAttribute());
        assertEquals("test", task.get(0).getValueAttribute());
    }

    @Test
    public void testCodeAreaFormValidation() {

        // check for null
        IPythonBuilder.DescriptorImpl descriptor = new IPythonBuilder.DescriptorImpl();
        FormValidation result = descriptor.doCheckCode("");
        assertEquals(result.kind, FormValidation.Kind.ERROR);

        // check a non null input
        FormValidation result1 = descriptor.doCheckCode("print('HI')");
        assertEquals(result1.kind, FormValidation.Kind.OK);
    }

    @Test
    public void testNotebook() throws Exception {

        Assume.assumeTrue(!Functions.isWindows());
        String repoURL = "https://github.com/Loghijiaha/simple-ml-pipeline-tutorial.git";
        List<UserRemoteConfig> repos = new ArrayList<>();
        repos.add(new UserRemoteConfig(repoURL, null, null, null));
        List<BranchSpec> branchSpecs = Collections.singletonList(new BranchSpec("master"));
        GitSCM scm = new GitSCM(
                repos,
                branchSpecs,
                false, Collections.<SubmoduleConfig>emptyList(),
                null, JGitTool.MAGIC_EXENAME,
                Collections.<GitSCMExtension>emptyList());
        project.setScm(scm);
        // created a builder and added
        IPythonBuilder builder = new IPythonBuilder("", "train_model.ipynb", "file", "notebook", "python");
        project.getBuildersList().add(builder);
        project.save();
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatusSuccess(build);


    }

}
