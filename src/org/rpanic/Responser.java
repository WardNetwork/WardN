package org.rpanic;

public interface Responser<T, S> {

	public boolean acceptable(T responseType) ;
	
	public void accept(T response, S socket);
	
}
