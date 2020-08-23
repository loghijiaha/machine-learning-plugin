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


import java.io.Serializable;
import java.util.Objects;

/**
 * The type Python user config.
 */
public class IPythonUserConfig implements Serializable {

    private static final long serialVersionUID = 7965902849225659175L;

    private final String kernel;
    private final long iPythonLaunchTimeout;
    private final long maxResult;
    private final String workingDirectory;

    /**
     * Constructor for configuration
     *
     * @param kernel               the kernel
     * @param iPythonLaunchTimeout the python launch timeout
     * @param maxResult            the max result
     * @param workingDirectory     the working directory
     */
    public IPythonUserConfig(String kernel, long iPythonLaunchTimeout, long maxResult, String workingDirectory) {
        this.kernel = kernel;
        this.iPythonLaunchTimeout = iPythonLaunchTimeout;
        this.maxResult = maxResult;
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets kernel
     *
     * @return kernel
     */
    public String getkernel() {
        return kernel;
    }

    /**
     * Gets ipython launch timeout.
     *
     * @return ipython launch timeout
     */
    public long getIPythonLaunchTimeout() {
        return iPythonLaunchTimeout;
    }

    /**
     * Gets max result.
     *
     * @return the max result
     */
    public long getMaxResult() {
        return maxResult;
    }

    /**
     * Gets working directory.
     *
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPythonUserConfig userConfig = (IPythonUserConfig) o;
        return kernel.equals(userConfig.getkernel()) &&
                iPythonLaunchTimeout == userConfig.getIPythonLaunchTimeout() &&
                maxResult == userConfig.getMaxResult() &&
                workingDirectory == userConfig.getWorkingDirectory();
    }

    @Override
    public int hashCode() {
        return Objects.hash(kernel, iPythonLaunchTimeout, maxResult, workingDirectory);
    }

}
