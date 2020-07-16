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

import io.jenkins.plugins.ml.KernelInterpreter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.zeppelin.display.GUI;
import org.apache.zeppelin.interpreter.*;
import org.apache.zeppelin.r.RInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class RKernelInterpreter implements KernelInterpreter {

    /**
     * Our logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RKernelInterpreter.class);

    private LazyOpenInterpreter interpreter;
    private InterpreterResultMessage interpreterResultMessage ;

    public RKernelInterpreter() {
        // properties for the interpreter
        Properties properties = new Properties();
        // Initiate a Lazy interpreter
        interpreter = new LazyOpenInterpreter(new RInterpreter(properties));

    }

    @Override
    public InterpreterResultMessage interpretCode(String code) throws InterpreterException {
        InterpreterResult result;
        InterpreterContext context = getInterpreterContext();
        result = interpreter.interpret(code, context);
        LOGGER.info(result.code().toString());
        JSONObject jsonObj = (JSONObject)  JSONSerializer.toJSON(result.toJson());
        JSONArray jsonArray = jsonObj.getJSONArray("msg");
        StringBuilder test_data = new StringBuilder();
        for (Object o : jsonArray) {
            JSONObject jObj = (JSONObject)  JSONSerializer.toJSON((o));
            if(jObj.getString("type").equals("TEXT")){
                test_data.append(jObj.getString("data")).append("\n");
            }
        }
        interpreterResultMessage = new InterpreterResultMessage(InterpreterResult.Type.TEXT,test_data.toString());
        return interpreterResultMessage;
    }

    @Override
    public void start() {
        try {
            interpreter.open();
        } catch (InterpreterException e) {
            LOGGER.error("Unsupported operation");
        }
    }

    @Override
    public void shutdown() {
        try {
            interpreter.close();
        } catch (InterpreterException e) {
            LOGGER.error("Unsupported operation for shutting down");
        }
    }

    private static InterpreterContext getInterpreterContext() {
        return new InterpreterContext.Builder()
                .setNoteId("noteID")
                .setParagraphId("paragraphId")
                .setReplName("replName")
                .setInterpreterOut(new InterpreterOutput(null))
                .setNoteGUI(new GUI())
                .setGUI(new GUI())
                .setAngularObjectRegistry(null)
                .setResourcePool(null)
                .build();

    }

    LazyOpenInterpreter getInterpreter() {
        return interpreter;
    }
}
