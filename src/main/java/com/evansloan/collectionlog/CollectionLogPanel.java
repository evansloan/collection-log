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
import java.util.List;


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

	private final int DEFAULT_WIDTH = PluginPanel.PANEL_WIDTH - 5;
	private final Dimension BUTTON_DIMENSION = new Dimension(DEFAULT_WIDTH, 30);
	private final EmptyBorder DEFAULT_BORDER = new EmptyBorder(10, 10, 10, 10);

	private final CollectionLogPlugin collectionLogPlugin;
	private final JPanel missingEntriesPanel;
	private final JPanel utilityBtnPanel;

	private JList<String> missingEntriesList;

	public CollectionLogPanel(CollectionLogPlugin collectionLogPlugin)
	{
		super(false);

		this.collectionLogPlugin = collectionLogPlugin;

		missingEntriesPanel = new JPanel();
		missingEntriesPanel.setLayout(new BorderLayout());
		missingEntriesPanel.setBorder(DEFAULT_BORDER);

		utilityBtnPanel = new JPanel();
		utilityBtnPanel.setLayout(new BorderLayout());
		utilityBtnPanel.setBorder(DEFAULT_BORDER);

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(DEFAULT_BORDER);
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
			"https://discord.gg/loghunters"
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
		add(utilityBtnPanel, BorderLayout.SOUTH);
		add(missingEntriesPanel, BorderLayout.CENTER);
	}

	public void loadLoggedInState()
	{
		missingEntriesPanel.removeAll();

		JTextArea openLogMessage = createTextArea("Open your collection log to start tracking your progress!");
		missingEntriesPanel.add(openLogMessage);

		missingEntriesPanel.repaint();
		missingEntriesPanel.revalidate();

		utilityBtnPanel.removeAll();
		utilityBtnPanel.repaint();
		utilityBtnPanel.revalidate();
	}

	public void loadLogOpenedState()
	{
		missingEntriesPanel.removeAll();

		JTextArea instructions = createTextArea("Click through each page in the collection log to save your current progress.");
		missingEntriesPanel.add(instructions, BorderLayout.NORTH);

		loadMissingEntries();
		loadUtilityButtons();

		missingEntriesPanel.repaint();
		missingEntriesPanel.revalidate();
	}

	private void loadUtilityButtons()
	{
		utilityBtnPanel.removeAll();

		JButton uploadDataBtn = createButton(
			"Upload collection log data",
			(action) -> collectionLogPlugin.getClientThread().invokeLater(collectionLogPlugin::saveCollectionLogData)
		);

		if (collectionLogPlugin.getConfig().allowApiConnections())
		{
			utilityBtnPanel.add(uploadDataBtn, BorderLayout.NORTH);
		}

		JButton recalcBtn = createButton(
			"Recalculate totals",
			(action) -> collectionLogPlugin.recalculateTotalCounts()
		);
		recalcBtn.setPreferredSize(BUTTON_DIMENSION);
		recalcBtn.addActionListener((event) -> collectionLogPlugin.recalculateTotalCounts());
		utilityBtnPanel.add(recalcBtn, BorderLayout.CENTER);

		JButton clearDataBtn = createButton(
			"Reset collection log data",
			(action) -> collectionLogPlugin.clearCollectionLogData()
		);
		utilityBtnPanel.add(clearDataBtn, BorderLayout.SOUTH);

		utilityBtnPanel.repaint();
		utilityBtnPanel.revalidate();
	}

	private void loadMissingEntries()
	{
		List<String> missingEntries = collectionLogPlugin.findMissingEntries();
		if (missingEntries == null)
		{
			return;
		}

		if (missingEntries.size() == 0)
		{
			missingEntriesPanel.removeAll();

			JTextArea entriesLoadedText = createTextArea("All collection log entries loaded!");
			missingEntriesPanel.add(entriesLoadedText, BorderLayout.NORTH);

			missingEntriesPanel.repaint();
			missingEntriesPanel.revalidate();
			return;
		}

		JPanel missingEntriesContainer = new JPanel();
		missingEntriesContainer.setLayout(new BorderLayout());

		JLabel missingEntriesLabel = new JLabel("Missing collection log entries");
		missingEntriesLabel.setBorder(DEFAULT_BORDER);
		missingEntriesLabel.setForeground(Color.WHITE);
		missingEntriesContainer.add(missingEntriesLabel, BorderLayout.NORTH);

		missingEntriesList = new JList<>();
		missingEntriesList.setListData(missingEntries.toArray(new String[0]));
		missingEntriesList.setLayout(new BorderLayout());
		missingEntriesList.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane listScrollContainer = new JScrollPane(missingEntriesList);
		missingEntriesContainer.add(listScrollContainer, BorderLayout.CENTER);

		missingEntriesPanel.add(missingEntriesContainer, BorderLayout.CENTER);
	}

	public void updateMissingEntriesList()
	{
		if (missingEntriesList == null)
		{
			return;
		}

		missingEntriesList.removeAll();

		List<String> missingEntries = collectionLogPlugin.findMissingEntries();

		if (missingEntries.size() == 0)
		{
			missingEntriesList.repaint();
			missingEntriesList.revalidate();
			loadMissingEntries();
			return;
		}

		missingEntriesList.setListData(missingEntries.toArray(new String[0]));

		missingEntriesList.repaint();
		missingEntriesList.revalidate();
	}

	private JTextArea createTextArea(String text)
	{
		JTextArea textArea = new JTextArea(5, 22);
		textArea.setText(text);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setOpaque(false);

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
		btn.setPreferredSize(BUTTON_DIMENSION);
		btn.addActionListener(actionListener);
		btn.setBorder(new EmptyBorder(5, 0, 5, 0));

		return btn;
	}
}
