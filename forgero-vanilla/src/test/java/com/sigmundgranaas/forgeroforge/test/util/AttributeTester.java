package com.sigmundgranaas.forgeroforge.test.util;

import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.state.State;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeTester {
    private final List<TestInfo> tests;
    private final State state;

    public AttributeTester(List<TestInfo> tests, State state) {
        this.tests = tests;
        this.state = state;
    }

    public AttributeTester( State state) {
        this.tests = new ArrayList<>();
        this.state = state;
    }

    public static AttributeTester tester(State state){
        return new AttributeTester(state);
    }

    public AttributeTester add(TestInfo info){
        this.tests.add(info);
        return this;
    }

    public AttributeTester add(AttributeType attribute, float target, float flex){
        this.tests.add(of(attribute, target, flex));
        return this;
    }

    public AttributeTester add(AttributeType attribute, Double target, float flex){
        this.tests.add(of(attribute,target.floatValue(), flex));
        return this;
    }

    public AttributeTester add(AttributeType attribute, Double target, Double flex){
        this.tests.add(of(attribute,target.floatValue(),flex.floatValue()));
        return this;
    }

    public AttributeTester add(AttributeType attribute, float target, Double flex){
        this.tests.add(of(attribute, target,flex.floatValue()));
        return this;
    }


    public void run(){
    tests.forEach(test -> {
        assertEquals(test.target, state.stream().applyAttribute(test.attribute), test.flex, test.attribute.toString());
    });
}
    public static TestInfo of(AttributeType attribute, float target, float flex){return new TestInfo(attribute, target, flex);}
    record TestInfo(AttributeType attribute, float target, float flex){

    };

}
