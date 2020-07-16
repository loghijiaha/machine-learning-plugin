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

package io.jenkins.plugins.ml.r;

import io.jenkins.plugins.ml.InterpreterManager;
import io.jenkins.plugins.ml.KernelInterpreter;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterException;
import org.apache.zeppelin.interpreter.InterpreterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class RInterpreterManager extends InterpreterManager {


    private static final Logger LOGGER = LoggerFactory.getLogger(RInterpreterManager.class);
    private static final InterpreterGroup mockInterpreterGroup = new InterpreterGroup();
    private static int sessionId = 0;
    private KernelInterpreter kernelInterpreter;

    public RInterpreterManager() {
        mockInterpreterGroup.put("session_" + sessionId, new ArrayList<Interpreter>());
    }

    public static void main(String[] args) throws InterpreterException, IOException {
        // Test for the interpreter
        RInterpreterManager ir = new RInterpreterManager();
        ir.initiateInterpreter();
        System.out.println(ir.invokeInterpreter("a <- 42\nprint(a)"));
        ir.close();

    }

    @Override
    public void initiateInterpreter()  {
        kernelInterpreter = createInterpreter();
        kernelInterpreter.start();
    }

    @Override
    public void closeInterpreter() {
        LOGGER.info(kernelInterpreter.toString());
        this.close();
    }

    @Override
    protected boolean testConnection() {
        //TODO
        return false;
    }

    /**
     * Used to create new RKernelInterpreters
     *
     * @return interpreter instance
     */
    @Override
    protected KernelInterpreter createInterpreter() {
        kernelInterpreter = new RKernelInterpreter();

        // zeppelin api for interpreter
        Interpreter interpreter = ((RKernelInterpreter) kernelInterpreter).getInterpreter();
        mockInterpreterGroup.get("session_" + sessionId).add(interpreter);
        interpreter.setInterpreterGroup(mockInterpreterGroup);
        sessionId += 1;
        return kernelInterpreter;
    }

    @Override
    public void close()  {
        kernelInterpreter.shutdown();
    }

    @Override
    protected String invokeInterpreter(String code) throws IOException, InterpreterException {
        return kernelInterpreter.interpretCode(code).toString();
    }

}
