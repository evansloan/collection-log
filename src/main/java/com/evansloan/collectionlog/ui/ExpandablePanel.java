package com.evansloan.collectionlog.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ExpandablePanel extends JPanel
{
	private final JPanel titlePanel;
	private final JLabel titleLabel;
	private final JComponent content;
	private boolean isExpanded;

	public ExpandablePanel(String title, JComponent content, boolean isExpanded)
	{
		this.isExpanded = isExpanded;
		this.content = content;

		setLayout(new BorderLayout(0, 1));
		setBorder(new EmptyBorder(10, 0, 0, 0));

		titlePanel = new JPanel();
		titlePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		titlePanel.setBackground(isExpanded ? ColorScheme.BRAND_ORANGE : ColorScheme.DARKER_GRAY_COLOR);
		titlePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		titlePanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				handleMouseClick();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				handleMouseHover(true);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				handleMouseHover(false);
			}
		});

		titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(isExpanded ? Color.BLACK : Color.WHITE);
		titlePanel.add(titleLabel);

		content.setVisible(isExpanded);

		add(titlePanel, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}

	public ExpandablePanel(String title, JComponent content)
	{
		this(title, content, false);
	}

	private void handleMouseClick()
	{
		isExpanded = !isExpanded;
		Color color = isExpanded ? ColorScheme.BRAND_ORANGE : ColorScheme.DARKER_GRAY_COLOR;
		Color textColor = isExpanded ? Color.BLACK : Color.WHITE;

		titlePanel.setBackground(color);
		titleLabel.setForeground(textColor);
		content.setVisible(isExpanded);
	}

	private void handleMouseHover(boolean isHovering)
	{
		Color color = ColorScheme.DARKER_GRAY_COLOR;

		if (isHovering)
		{
			color = ColorScheme.DARKER_GRAY_HOVER_COLOR;
			if (isExpanded)
			{
				color = ColorScheme.BRAND_ORANGE.darker();
			}
		}
		else if (isExpanded)
		{
			color = ColorScheme.BRAND_ORANGE;
		}

		titlePanel.setBackground(color);
	}
}
