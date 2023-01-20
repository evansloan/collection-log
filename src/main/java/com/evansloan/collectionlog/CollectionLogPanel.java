package com.evansloan.collectionlog;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;


@Slf4j
public class CollectionLogPanel extends PluginPanel
{
	private static final ImageIcon ACCOUNT_ICON;
	private static final ImageIcon DISCORD_ICON;
	private static final ImageIcon GITHUB_ICON;
	private static final ImageIcon HELP_ICON;
	private static final ImageIcon INFO_ICON;
	private static final ImageIcon SETTINGS_ICON;
	private static final ImageIcon WEBSITE_ICON;

	static
	{
		ACCOUNT_ICON = Icon.ACCOUNT.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		DISCORD_ICON = Icon.DISCORD.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		GITHUB_ICON = Icon.GITHUB.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		HELP_ICON = Icon.HELP.getIcon(img -> ImageUtil.resizeImage(img, 20, 20));
		INFO_ICON = Icon.INFO.getIcon(img -> ImageUtil.resizeImage(img, 20, 20));
		SETTINGS_ICON = Icon.SETTINGS.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		WEBSITE_ICON = Icon.COLLECTION_LOG.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
	}

	private final int DEFAULT_WIDTH = PluginPanel.PANEL_WIDTH - 5;
	private final Dimension BUTTON_DIMENSION = new Dimension(DEFAULT_WIDTH, 30);
	private final EmptyBorder DEFAULT_BORDER = new EmptyBorder(10, 10, 10, 10);

	private final CollectionLogPlugin collectionLogPlugin;
	private final ClientThread clientThread;
	private final CollectionLogConfig config;
	private final EventBus eventBus;

	private JPanel accountPanel;
	private JPanel helpPanel;
	private JPanel infoPanel;
	private JPanel settingsPanel;

	private JLabel clnEnabledLabel;

	@Inject
	public CollectionLogPanel(CollectionLogPlugin collectionLogPlugin, ClientThread clientThread, CollectionLogConfig config, EventBus eventBus)
	{
		super(false);

		this.collectionLogPlugin = collectionLogPlugin;
		this.clientThread = clientThread;
		this.config = config;
		this.eventBus = eventBus;
	}

	public void create()
	{
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());
		setBorder(DEFAULT_BORDER);

		JPanel layoutPanel = new JPanel();
		layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));

		JPanel titlePanel = createTitlePanel();
		JPanel tabPanel = createTabPanel();

		layoutPanel.add(titlePanel);
		layoutPanel.add(tabPanel);

		add(layoutPanel, BorderLayout.NORTH);

		eventBus.register(this);
	}

	public void destroy()
	{
		eventBus.unregister(this);
	}

	private JPanel createTitlePanel()
	{
		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
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

		return titleContainer;
	}

	private JPanel createTabPanel()
	{
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));

		JPanel tabContentPanel = new JPanel();
		tabContentPanel.setLayout(new BoxLayout(tabContentPanel, BoxLayout.Y_AXIS));
		MaterialTabGroup tabGroup = new MaterialTabGroup(tabContentPanel);
		tabGroup.setLayout(new GridLayout(1, 4, 10, 10));
		tabGroup.setBorder(new EmptyBorder(0, 0, 10, 0));
		tabPanel.add(tabGroup);
		tabPanel.add(tabContentPanel);

		infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		tabContentPanel.add(infoPanel);

		JPanel pluginInfoPanel = new JPanel(new GridLayout(0, 1));
		pluginInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		pluginInfoPanel.setBorder(DEFAULT_BORDER);
		infoPanel.add(pluginInfoPanel);

		JLabel versionLabel = createKeyValueLabel("Version: ", "2.4.0");
		clnEnabledLabel = createKeyValueLabel(
			"collectionlog.net uploads: ",
			config.allowApiConnections() ? "Enabled" : "Disabled"
		);
		pluginInfoPanel.add(versionLabel);
		pluginInfoPanel.add(clnEnabledLabel);

		ExpandablePanel testPanel = new ExpandablePanel("Test", new JLabel("Test"));
		infoPanel.add(testPanel);

		JPanel quickStartContent = new JPanel(new GridLayout(0, 1));
		quickStartContent.setBorder(DEFAULT_BORDER);
		quickStartContent.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		String quickStartText = "The collection log plugin allows you to sync your in-game collection log data " +
			"with collectionlog.net.\n\n" +
			"To get started, first enable the collectionlog.net connections plugin config.\n\n" +
			"After enabling the config, open your collection log and click through each page in the log in order " +
			"for the plugin to grab your collection log data. Pages that haven't been loaded into the plugin will " +
			"be marked with * in your collection log.\n\n" +
			"Once all the collection log pages have been clicked through, your data can be uploaded by clicking " +
			"on the upload button in the account tab in the plugin side panel. Your data will also be uploaded " +
			"automatically upon log out.";
		JTextArea quickStartTextArea = createTextArea(quickStartText);
		quickStartContent.add(quickStartTextArea);
		ExpandablePanel quickStartPanel = new ExpandablePanel("Quick start", quickStartContent, true);
		infoPanel.add(quickStartPanel);

		createTab(INFO_ICON, "Info", tabGroup, infoPanel).select();

		accountPanel = new JPanel();
		JButton uploadDataBtn = createButton(
			"Upload collection log data",
			(action) -> clientThread.invokeLater(collectionLogPlugin::saveCollectionLogData)
		);
		accountPanel.add(uploadDataBtn, BorderLayout.NORTH);
		JLabel accountSubLabel = new JLabel("<html>Log in to manage your<br>collectionlog.net account<html>");
		accountSubLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		accountPanel.add(accountSubLabel);
		createTab(ACCOUNT_ICON, "Account", tabGroup, accountPanel);

		settingsPanel = new JPanel();
		JLabel settingsLabel = new JLabel("Log in to view settings");
		settingsLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		settingsPanel.add(settingsLabel);
		createTab(SETTINGS_ICON, "Settings", tabGroup, settingsPanel);

		helpPanel = new JPanel();
		JLabel helpLabel = new JLabel("Help");
		helpPanel.add(helpLabel);
		createTab(HELP_ICON, "Help", tabGroup, helpLabel);

		return tabPanel;
	}

	private void loadUtilityButtons()
	{
//		JButton uploadDataBtn = createButton(
//			"Upload collection log data",
//			(action) -> collectionLogPlugin.getClientThread().invokeLater(collectionLogPlugin::saveCollectionLogData)
//		);

//		if (collectionLogPlugin.getConfig().allowApiConnections())
//		{
//			utilityBtnPanel.add(uploadDataBtn, BorderLayout.NORTH);
//		}

//		JButton recalcBtn = createButton(
//			"Recalculate totals",
//			(action) -> collectionLogPlugin.recalculateTotalCounts()
//		);
//		recalcBtn.setPreferredSize(BUTTON_DIMENSION);
//		recalcBtn.addActionListener((event) -> collectionLogPlugin.recalculateTotalCounts());
//		utilityBtnPanel.add(recalcBtn, BorderLayout.CENTER);
//
//		JButton clearDataBtn = createButton(
//			"Reset collection log data",
//			(action) -> collectionLogPlugin.clearCollectionLogData()
//		);
//		utilityBtnPanel.add(clearDataBtn, BorderLayout.SOUTH);
//
//		utilityBtnPanel.repaint();
//		utilityBtnPanel.revalidate();
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

	private MaterialTab createTab(ImageIcon icon, String toolTipText, MaterialTabGroup tabGroup, JComponent content)
	{
		MaterialTab tab = new MaterialTab(icon, tabGroup, content);
		tab.setToolTipText(toolTipText);
		tabGroup.addTab(tab);

		return tab;
	}

	private String htmlText(String text)
	{
		return "<html><body style='color:#A5A5A5'>" + text + "</body></html>";
	}

	private String highlightText(String text)
	{
		return "<span style='color:#DC8A00'>" + text + "</span>";
	}

	private String getKeyValueText(String key, String value)
	{
		return htmlText(key + "<span style='color:white'>" + value + "</span>");
	}

	private JLabel createKeyValueLabel(String key, String value)
	{
		return new JLabel(getKeyValueText(key, value));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getKey().equals("upload_collection_log"))
		{
			clnEnabledLabel.setText(getKeyValueText(
				"collectionlog.net uploads: ",
				config.allowApiConnections() ? "Enabled" : "Disabled"
			));
		}
	}
}
