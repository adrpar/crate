/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.operation.scalar.cast;

import com.google.common.collect.ImmutableList;
import io.crate.metadata.FunctionIdent;
import io.crate.operation.Input;
import io.crate.operation.scalar.AbstractScalarFunctionsTest;
import io.crate.planner.symbol.Function;
import io.crate.planner.symbol.Literal;
import io.crate.planner.symbol.Symbol;
import io.crate.testing.TestingHelpers;
import io.crate.types.DataType;
import io.crate.types.DataTypes;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class ToShortFunctionTest extends AbstractScalarFunctionsTest {

    private final String functionName = CastFunctionResolver.FunctionNames.TO_SHORT;

    private ToPrimitiveFunction getFunction(DataType type) {
        return (ToPrimitiveFunction) functions.get(new FunctionIdent(functionName, Arrays.asList(type)));
    }

    private Short evaluate(Object value, DataType type) {
        Input[] input = {(Input)Literal.newLiteral(type, value)};
        return (Short) getFunction(type).evaluate(input);
    }

    private Symbol normalize(Object value, DataType type) {
        ToPrimitiveFunction function = getFunction(type);
        return function.normalizeSymbol(new Function(function.info(),
                Arrays.<Symbol>asList(Literal.newLiteral(type, value))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNormalizeSymbol() throws Exception {
        TestingHelpers.assertLiteralSymbol(normalize("123", DataTypes.STRING), (short)123);
        TestingHelpers.assertLiteralSymbol(normalize(12.5f, DataTypes.FLOAT), (short)12);
    }

    @Test
    public void testEvaluate() throws Exception {
        assertThat(evaluate("123", DataTypes.STRING), is((short)123));
        assertThat(evaluate(null, DataTypes.STRING), nullValue());
        assertThat(evaluate(123.5f, DataTypes.FLOAT), is((short)123));
        assertThat(evaluate(123.5d, DataTypes.DOUBLE), is((short) 123));
        assertThat(evaluate(null, DataTypes.FLOAT), nullValue());
        assertThat(evaluate(42L, DataTypes.LONG), is((short)42));
        assertThat(evaluate(null, DataTypes.INTEGER), nullValue());
    }

    @Test
    public void testInvalidType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("type 'object' not supported for conversion");
        functions.get(new FunctionIdent(functionName, ImmutableList.<DataType>of(DataTypes.OBJECT)));
    }

    @Test
    public void testNormalizeInvalidString() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("cannot cast 'hello' to short");
        normalize("hello", DataTypes.STRING);
    }
}
