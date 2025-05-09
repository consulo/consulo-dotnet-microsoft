package consulo.dotnet.microsoft.debugger.proxy;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import mssdw.MirrorWithId;
import mssdw.Value;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public abstract class MicrosoftValueProxyBase<T extends Value<?>> implements DotNetValueProxy
{
	protected T myValue;

	public MicrosoftValueProxyBase(T value)
	{
		myValue = value;
	}

	@Override
	public boolean isEqualTo(@Nonnull DotNetValueProxy proxy)
	{
		if(proxy instanceof MicrosoftValueProxyBase)
		{
			Value<?> value = ((MicrosoftValueProxyBase<?>) proxy).myValue;
			if(value instanceof MirrorWithId && myValue instanceof MirrorWithId)
			{
				return ((MirrorWithId) value).id() == ((MirrorWithId) myValue).id();
			}
		}
		return false;
	}

	public T getMirror()
	{
		return myValue;
	}

	@Nullable
	@Override
	public DotNetTypeProxy getType()
	{
		return MicrosoftTypeProxy.of(myValue.type());
	}

	@Nonnull
	@Override
	public Object getValue()
	{
		return myValue.value();
	}
}
