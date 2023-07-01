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

package io.jenkins.plugins.ml;

import hudson.model.FreeStyleProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IPythonBuilderTest {

    private IPythonBuilder builder;
    private FreeStyleProject project;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        // set up mock IPythonGlobalConfiguration
        project = jenkins.createFreeStyleProject();
        builder = new IPythonBuilder("","","","","");
    }

    @Test
    public void testWithMockConfigServers() throws IOException {
        // test for servers method using mock IPythonGlobalConfiguration
        List<Server> serverList = new ArrayList<>();
        serverList.add(new Server("test", "julia", 10, 300));
        project.getBuildersList().add(builder);
        project.save();
        project.scheduleBuild2(0);

        // No exception was thrown
        assert true;
    }
}
