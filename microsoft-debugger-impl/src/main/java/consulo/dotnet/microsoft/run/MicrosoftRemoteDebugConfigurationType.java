package consulo.dotnet.microsoft.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.debugger.impl.DotNetDebugProcessBase;
import consulo.dotnet.debugger.impl.runner.remote.DotNetRemoteConfiguration;
import consulo.dotnet.microsoft.debugger.MicrosoftDebugProcess;
import consulo.dotnet.microsoft.debugger.localize.MicrosoftDebuggerLocalize;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.debug.XDebugSession;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.project.Project;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 31/12/2020
 */
@ExtensionImpl
public class MicrosoftRemoteDebugConfigurationType extends ConfigurationTypeBase
{
	public MicrosoftRemoteDebugConfigurationType()
	{
		super("MicrosoftRemoteDebugConfigurationType", MicrosoftDebuggerLocalize.microsoftDebuggerRemoteConfigurationName(), PlatformIconGroup.runconfigurationsRemote());

		addFactory(new ConfigurationFactory(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new DotNetRemoteConfiguration(project, this)
				{
					@Nonnull
					@Override
					public DotNetDebugProcessBase createDebuggerProcess(@Nonnull XDebugSession session, @Nonnull DebugConnectionInfo info) throws ExecutionException
					{
						MicrosoftDebugProcess process = new MicrosoftDebugProcess(session, this, info);
						//						process.getDebugThread().addListener(new MonoVirtualMachineListener()
						//						{
						//							@Override
						//							public void connectionSuccess(@Nonnull VirtualMachine machine)
						//							{
						//								ProcessHandler processHandler = process.getProcessHandler();
						//								processHandler.notifyTextAvailable(String.format("Success attach to %s:%d", info.getHost(), info.getPort()), ProcessOutputTypes.STDOUT);
						//							}
						//
						//							@Override
						//							public void connectionStopped()
						//							{
						//							}
						//
						//							@Override
						//							public void connectionFailed()
						//							{
						//								ProcessHandler processHandler = process.getProcessHandler();
						//								processHandler.notifyTextAvailable(String.format("Failed attach to %s:%d", info.getHost(), info.getPort()), ProcessOutputTypes.STDERR);
						//								StopProcessAction.stopProcess(processHandler);
						//							}
						//						});
						return process;
					}
				};
			}

			@Override
			public boolean isApplicable(@Nonnull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(DotNetModuleExtension.class);
			}
		});
	}
}
