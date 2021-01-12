/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.heretere.hdl.dependency.builder;

import com.heretere.hdl.dependency.Dependency;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base level implementation of DependencyBuilder.
 *
 * @param <T> The dependency type.
 */
public interface DependencyBuilder<T extends Dependency> {
    /**
     * Add a dependency to the dependency builder.
     *
     * @param dependency The dependency to add.
     * @return this
     */
    @Contract("_ -> this")
    @NotNull DependencyBuilder<T> dependency(@NotNull T dependency);

    /**
     * Creates a new {@link DependencyProvider} from the builder.
     *
     * @return A new {@link DependencyProvider}.
     */
    @Contract("-> new")
    @NotNull DependencyProvider<T> build();
}
