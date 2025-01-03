package consulo.dotnet.microsoft.debugger.proxy;

import consulo.component.util.pointer.Named;
import consulo.dotnet.debugger.proxy.DotNetVariableProxy;
import mssdw.MirrorWithIdAndName;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public abstract class MicrosoftVariableProxyBase<T extends MirrorWithIdAndName> implements Named, DotNetVariableProxy
{
	protected T myMirror;

	public MicrosoftVariableProxyBase(@Nonnull T mirror)
	{
		myMirror = mirror;
	}

	@Nonnull
	public T getMirror()
	{
		return myMirror;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof MicrosoftVariableProxyBase && myMirror.equals(((MicrosoftVariableProxyBase) obj).myMirror);
	}

	@Override
	public int hashCode()
	{
		return myMirror.hashCode();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return myMirror.name();
	}
}
