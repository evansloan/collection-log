/*
 *
 *  * Copyright (c) 2021, Senmori
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.evansloan.collectionlog.ui;

import com.evansloan.collectionlog.CollectionLogPlugin;
import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import net.runelite.client.util.ImageUtil;

public enum Icon
{
	ACCOUNT("/account.png"),
	COLLECTION_LOG("/collection_log.png"),
	COLLECTION_LOG_TOOLBAR("/collection_log_toolbar.png"),
	DISCORD("/discord.png"),
	GITHUB("/github.png"),
	RANDOM("/random.png"),
	INFO("/info.png"),
	;

	private final String file;
	Icon(String file)
	{
		this.file = file;
	}

	/**
	 * Get the raw {@link BufferedImage} of this icon.
	 * @return {@link BufferedImage} of the icon
	 */
	public BufferedImage getImage()
	{
		return ImageUtil.loadImageResource(CollectionLogPlugin.class, file);
	}

	/**
	 * @return the {@link ImageIcon} with no modifications. Equivalent to {@code getIcon(UnaryOperator.identity())}
	 */
	public ImageIcon getIcon()
	{
		return getIcon(UnaryOperator.identity());
	}

	/**
	 * Return this icon.
	 * <br>
	 * The {@link UnaryOperator} is applied to the {@link BufferedImage}. The {@link ImageIcon}
	 * is then created using that modified image.
	 *
	 * @param func the {@link UnaryOperator} to apply to the image
	 * @return the modified {@link ImageIcon}
	 */
	public ImageIcon getIcon(@Nonnull UnaryOperator<BufferedImage> func)
	{
		BufferedImage img = func.apply(getImage());
		return new ImageIcon(img);
	}
}