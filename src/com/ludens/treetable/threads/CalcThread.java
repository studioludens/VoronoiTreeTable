package com.ludens.treetable.threads;

public class CalcThread extends Thread {

	public boolean running = false;
	protected String threadName = "Calc";
	
	// are the values in this thread dirty?
	public boolean dirty = false;
	
	public void start(){
		dirty = false;
		
		super.start();
		
	}
	
	public void quit(){
		running = false;
		
		System.out.println("[THREAD:" + threadName + "] Quitting...");
		
		interrupt();
		
	}
	
	protected void trace(String text){
		System.out.println("[THREAD:" + threadName + "] " + text);
	}
}
