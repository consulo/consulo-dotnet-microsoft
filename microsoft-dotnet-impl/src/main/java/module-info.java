/**
 * @author VISTALL
 * @since 13-Jun-22
 */
module consulo.dotnet.microsoft
{
	// TODO remove this in future
	requires java.desktop;

	requires consulo.language.api;
	requires consulo.process.api;
	requires consulo.execution.api;
	requires consulo.execution.debug.api;
	requires consulo.dotnet.api;
	requires consulo.dotnet.psi.impl;

	requires com.sun.jna;
	requires com.sun.jna.platform;
	requires consulo.util.jna;

	requires consulo.dotnet.debugger.impl;
	requires consulo.dotnet.microsoft.debugger.impl;

	exports consulo.dotnet.microsoft.icon;
	exports consulo.microsoft.dotnet.module.extension;
	exports consulo.microsoft.dotnet.sdk;
	exports consulo.microsoft.dotnet.util;
}