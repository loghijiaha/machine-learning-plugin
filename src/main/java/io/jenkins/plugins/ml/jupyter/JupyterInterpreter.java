/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jenkins.plugins.ml.jupyter;

import org.apache.zeppelin.interpreter.*;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.interpreter.thrift.InterpreterCompletion;
import org.apache.zeppelin.jupyter.JupyterZeppelinContext;

import java.util.*;

public class JupyterInterpreter extends AbstractInterpreter {
    private Map<String, io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter> kernelInterpreterMap = new HashMap();

    public JupyterInterpreter(Properties properties) {
        super(properties);
    }

    public ZeppelinContext getZeppelinContext() {
        return new JupyterZeppelinContext(this.getInterpreterGroup().getInterpreterHookRegistry(), 1000);
    }

    protected InterpreterResult internalInterpret(String st, InterpreterContext context) throws InterpreterException {
        String kernel = (String) context.getLocalProperties().get("kernel");
        if (kernel == null) {
            return new InterpreterResult(Code.ERROR, "No kernel is specified");
        } else {
            io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter kernelInterpreter = null;
            synchronized (this.kernelInterpreterMap) {
                if (this.kernelInterpreterMap.containsKey(kernel)) {
                    kernelInterpreter = (io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter) this.kernelInterpreterMap.get(kernel);
                } else {
                    kernelInterpreter = new io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter(kernel, this.properties);
                    kernelInterpreter.open();
                    this.kernelInterpreterMap.put(kernel, kernelInterpreter);
                }
            }

            return kernelInterpreter.interpret(st, context);
        }
    }

    public void open() throws InterpreterException {
    }

    public void close() throws InterpreterException {
        Iterator var1 = this.kernelInterpreterMap.values().iterator();

        while (var1.hasNext()) {
            io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter kernelInterpreter = (io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter) var1.next();
            kernelInterpreter.close();
        }

    }

    public void cancel(InterpreterContext context) throws InterpreterException {
        String kernel = (String) context.getLocalProperties().get("kernel");
        if (kernel == null) {
            throw new InterpreterException("No kernel is specified");
        } else {
            io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter kernelInterpreter = (io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter) this.kernelInterpreterMap.get(kernel);
            if (kernelInterpreter == null) {
                throw new InterpreterException("No such interpreter: " + kernel);
            } else {
                kernelInterpreter.cancel(context);
            }
        }
    }

    public FormType getFormType() throws InterpreterException {
        return FormType.NATIVE;
    }

    public int getProgress(InterpreterContext context) throws InterpreterException {
        String kernel = (String) context.getLocalProperties().get("kernel");
        if (kernel == null) {
            throw new InterpreterException("No kernel is specified");
        } else {
            io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter kernelInterpreter = (io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter) this.kernelInterpreterMap.get(kernel);
            if (kernelInterpreter == null) {
                throw new InterpreterException("No such interpreter: " + kernel);
            } else {
                return kernelInterpreter.getProgress(context);
            }
        }
    }

    public List<InterpreterCompletion> completion(String buf, int cursor, InterpreterContext context) throws InterpreterException {
        String kernel = (String) context.getLocalProperties().get("kernel");
        if (kernel == null) {
            throw new InterpreterException("No kernel is specified");
        } else {
            io.jenkins.plugins.ml.jupyter.JupyterKernelInterpreter kernelInterpreter = (JupyterKernelInterpreter) this.kernelInterpreterMap.get(kernel);
            if (kernelInterpreter == null) {
                throw new InterpreterException("No such interpreter: " + kernel);
            } else {
                return kernelInterpreter.completion(buf, cursor, context);
            }
        }
    }
}
