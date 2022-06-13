/**
 * @author VISTALL
 * @since 13-Jun-22
 */
module consulo.dotnet.microsoft.debugger.impl
{
	requires consulo.ide.api;
	requires consulo.dotnet.api;

	requires mssdw.java.client;

	requires consulo.dotnet.debugger.api;
	requires consulo.dotnet.debugger.impl;

	exports consulo.dotnet.microsoft.debugger.breakpoint;
	exports consulo.dotnet.microsoft.debugger.proxy;
	exports consulo.dotnet.microsoft.run;
	exports consulo.dotnet.microsoft.debugger;
}