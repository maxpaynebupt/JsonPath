package com.jayway.jsonpath.reader.filter;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.eval.ExpressionEvaluator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 11/5/11
 * Time: 12:35 AM
 */
public class ArrayEvalFilter extends Filter {

    public static final Pattern PATTERN = Pattern.compile("(.*?)\\s?([=<>]+)\\s?(.*)");

    public ArrayEvalFilter(String condition) {
        super(condition);
    }

    @Override
    public Object filter(Object obj) {
        //[?(@.isbn = 10)]
        List<Object> src = toList(obj);
        List<Object> result = new LinkedList<Object>();

        String trimmedCondition = trim(condition, 5, 2);

        ConditionStatement conditionStatement = createConditionStatement(trimmedCondition);


        for (Object item : src) {
            if (isMatch(item, conditionStatement)) {
                result.add(item);
            }
        }
        return result;
    }

    private boolean isMatch(Object check, ConditionStatement conditionStatement) {
        if (!isMap(check)) {
            return false;
        }
        Map obj = toMap(check);

        if (!obj.containsKey(conditionStatement.getField())) {
            return false;
        }

        Object propertyValue = obj.get(conditionStatement.getField());

        if (isContainer(propertyValue)) {
            return false;
        }
        return ExpressionEvaluator.eval(propertyValue, conditionStatement.getOperator(), conditionStatement.getExpected());
    }


    private ConditionStatement createConditionStatement(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (matcher.matches()) {
            String property = matcher.group(1);
            String operator = matcher.group(2);
            String expected = matcher.group(3);

            return new ConditionStatement(property, operator, expected);
        } else {
            throw new InvalidPathException("Invalid match " + str);
        }
    }

    private class ConditionStatement {
        private String field;
        private String operator;
        private String expected;

        private ConditionStatement(String field, String operator, String expected) {
            this.field = field;
            this.operator = operator;
            this.expected = expected;
        }

        public String getField() {
            return field;
        }

        public String getOperator() {
            return operator;
        }

        public String getExpected() {
            return expected;
        }
    }
}