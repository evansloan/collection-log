package com.evansloan.collectionlog;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;


@Slf4j
public class CollectionLogPanel extends PluginPanel
{
	private static final ImageIcon DISCORD_ICON;
	private static final ImageIcon GITHUB_ICON;
	private static final ImageIcon WEBSITE_ICON;

	static
	{
		DISCORD_ICON = Icon.DISCORD.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		GITHUB_ICON = Icon.GITHUB.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		WEBSITE_ICON = Icon.COLLECTION_LOG.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
	}

	private final Dimension DEFAULT_DIMENSIONS = new Dimension(PluginPanel.PANEL_WIDTH - 5, 30);
	private final CollectionLogPlugin collectionLogPlugin;
	private final JPanel utilityBtnPanel;

	public CollectionLogPanel(CollectionLogPlugin collectionLogPlugin)
	{
		super(false);

		this.collectionLogPlugin = collectionLogPlugin;

		utilityBtnPanel = new JPanel();
		utilityBtnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		titlePanel.setLayout(new BorderLayout());

		JLabel title = new JLabel();
		title.setText("Collection Log");
		title.setForeground(Color.WHITE);
		titlePanel.add(title, BorderLayout.WEST);

		final JPanel infoButtons = new JPanel(new GridLayout(1, 3, 10, 0));
		infoButtons.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton websiteBtn = createTitleButton(
			WEBSITE_ICON,
			"Open collectionlog.net",
			"https://collectionlog.net"
		);
		infoButtons.add(websiteBtn);

		JButton discordBtn = createTitleButton(
			DISCORD_ICON,
			"Join the Log Hunters Discord Server",
			"https://discord.gg/cFVa9BRSEN"
		);
		infoButtons.add(discordBtn);

		JButton githubBtn = createTitleButton(
			GITHUB_ICON,
			"View the Collection Log plugin source code on GitHub",
			"https://github.com/evansloan/collection-log"
		);
		infoButtons.add(githubBtn);

		titlePanel.add(infoButtons, BorderLayout.EAST);

		JPanel titleContainer = new JPanel();
		titleContainer.setLayout(new BorderLayout());
		titleContainer.add(titlePanel, BorderLayout.NORTH);

		add(titleContainer, BorderLayout.NORTH);
		add(utilityBtnPanel, BorderLayout.CENTER);
	}

	public void loadDefaultState()
	{
		utilityBtnPanel.removeAll();

		JLabel defaultState = new JLabel();
		defaultState.setText("Log in to view collection log information");
		defaultState.setForeground(Color.WHITE);

		utilityBtnPanel.add(defaultState);
		utilityBtnPanel.repaint();
		utilityBtnPanel.revalidate();
	}

	public void loadUtilityButtons()
	{
		utilityBtnPanel.removeAll();

		if (collectionLogPlugin.getCollectionLogData() == null)
		{
			JTextArea openLogMessage = createTextArea("Open your collectionLog to start tracking your progress!");

			utilityBtnPanel.add(openLogMessage);
			utilityBtnPanel.repaint();
			utilityBtnPanel.revalidate();

			return;
		}

		JTextArea instructions = createTextArea("Click through each entry in the collection log to save your current progress.");
		utilityBtnPanel.add(instructions);

		JButton uploadDataBtn = createButton(
			"Upload collection log data",
			(action) -> collectionLogPlugin.getClientThread().invokeLater(collectionLogPlugin::saveCollectionLogData)
		);

		if (collectionLogPlugin.getConfig().uploadCollectionLog())
		{
			utilityBtnPanel.add(uploadDataBtn);
		}

		JButton recalcBtn = createButton(
			"Recalculate totals",
			(action) -> collectionLogPlugin.recalculateTotalCounts()
		);
		recalcBtn.setPreferredSize(DEFAULT_DIMENSIONS);
		recalcBtn.addActionListener((event) -> collectionLogPlugin.recalculateTotalCounts());
		utilityBtnPanel.add(recalcBtn);

		JButton clearDataBtn = createButton(
			"Reset collection log data",
			(action) -> collectionLogPlugin.clearCollectionLogData()
		);
		utilityBtnPanel.add(clearDataBtn);

		utilityBtnPanel.repaint();
		utilityBtnPanel.revalidate();
	}

	private JTextArea createTextArea(String text)
	{
		JTextArea textArea = new JTextArea(5, 22);
		textArea.setText(text);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFocusable(false);

		return textArea;
	}

	private JButton createTitleButton(ImageIcon icon, String toolTip, String url)
	{
		JButton btn = new JButton();
		SwingUtil.removeButtonDecorations(btn);
		btn.setIcon(icon);
		btn.setToolTipText(toolTip);
		btn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		btn.setUI(new BasicButtonUI());
		btn.addActionListener((event) -> LinkBrowser.browse(url));
		btn.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseEntered(java.awt.event.MouseEvent mouseEvent)
			{
				btn.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
			}

			public void mouseExited(java.awt.event.MouseEvent mouseEvent)
			{
				btn.setBackground(ColorScheme.DARK_GRAY_COLOR);
			}
		});

		return btn;
	}

	private JButton createButton(String text, ActionListener actionListener)
	{
		JButton btn = new JButton();
		btn.setText(text);
		btn.setForeground(Color.WHITE);
		btn.setPreferredSize(DEFAULT_DIMENSIONS);
		btn.addActionListener(actionListener);

		return btn;
	}
}
