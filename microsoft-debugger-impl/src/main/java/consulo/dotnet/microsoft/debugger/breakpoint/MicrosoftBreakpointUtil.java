package consulo.dotnet.microsoft.debugger.breakpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationManager;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.debugger.impl.breakpoint.DotNetBreakpointUtil;
import consulo.dotnet.debugger.impl.breakpoint.properties.DotNetExceptionBreakpointProperties;
import consulo.dotnet.microsoft.debugger.proxy.MicrosoftVirtualMachineProxy;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.language.util.ModuleUtilCore;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import mssdw.DebugInformationResult;
import mssdw.VirtualMachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 5/10/2016
 */
public class MicrosoftBreakpointUtil
{
	@Nullable
	@RequiredReadAction
	public static String getModulePath(@Nonnull Project project, @Nonnull XLineBreakpoint<?> breakpoint)
	{
		VirtualFile breakpointFile = VirtualFileManager.getInstance().findFileByUrl(breakpoint.getFileUrl());
		if(breakpointFile == null)
		{
			return null;
		}

		DotNetModuleExtension extension = ModuleUtilCore.getExtension(project, breakpointFile, DotNetModuleExtension.class);
		if(extension != null)
		{
			return DotNetMacroUtil.expandOutputFile(extension);
		}
		return null;
	}

	public static void createBreakpointRequest(@Nonnull final XDebugSession debugSession, @Nonnull MicrosoftVirtualMachineProxy virtualMachine, @Nonnull final XLineBreakpoint breakpoint)
	{
		String modulePath = ApplicationManager.getApplication().runReadAction((Supplier<String>) () -> getModulePath(debugSession.getProject(), breakpoint));

		// if we can't resolve module path or module is not loaded - breakpoint is not valid
		if(modulePath == null || !virtualMachine.getLoadedModules().contains(modulePath))
		{
			DotNetBreakpointUtil.updateLineBreakpointIcon(debugSession.getProject(), Boolean.FALSE, breakpoint);
			return;
		}

		VirtualMachine delegate = virtualMachine.getDelegate();

		DebugInformationResult result = delegate.findDebugOffset(breakpoint.getPresentableFilePath(), breakpoint.getLine() + 1, -1);
		// we can't resolve debug information
		if(result == null)
		{
			DotNetBreakpointUtil.updateLineBreakpointIcon(debugSession.getProject(), Boolean.FALSE, breakpoint);
			return;
		}

		delegate.eventRequestManager().createBreakpointRequest(result).enable();
		DotNetBreakpointUtil.updateLineBreakpointIcon(debugSession.getProject(), Boolean.TRUE, breakpoint);
	}

	public static void createExceptionRequest(MicrosoftVirtualMachineProxy virtualMachine, XBreakpoint<DotNetExceptionBreakpointProperties> breakpoint)
	{
		//TODO [VISTALL] unsupported for now
	}
}
