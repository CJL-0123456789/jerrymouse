package net.happygod.jerry.server;

public abstract class Servlet
{
	public void init(){}
	public void doGet(Request request,Response response){}
	public void doPost(Request request,Response response){}
}
