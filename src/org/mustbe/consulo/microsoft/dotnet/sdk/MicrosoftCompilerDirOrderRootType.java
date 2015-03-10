/*
 * Copyright 2013-2015 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.microsoft.dotnet.sdk;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.roots.OrderRootType;

/**
 * @author VISTALL
 * @since 11.03.2015
 */
public class MicrosoftCompilerDirOrderRootType extends OrderRootType
{
	@NotNull
	@LazyInstance
	public static MicrosoftCompilerDirOrderRootType getInstance()
	{
		return getOrderRootType(MicrosoftCompilerDirOrderRootType.class);
	}

	private MicrosoftCompilerDirOrderRootType()
	{
		super("microsoft-compiler-dirs");
	}
}