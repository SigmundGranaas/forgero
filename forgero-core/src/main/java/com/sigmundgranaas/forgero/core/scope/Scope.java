package com.sigmundgranaas.forgero.core.scope;

public interface Scope {
	String identifier();
	static Scope simple(String name){
		 return new Scope() {
			 @Override
			 public String identifier() {
				 return name;
			 }
		 };
	}
}
