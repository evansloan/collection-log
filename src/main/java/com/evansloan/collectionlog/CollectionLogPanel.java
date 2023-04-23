package com.evansloan.collectionlog;

import com.evansloan.collectionlog.ui.ExpandablePanel;
import com.evansloan.collectionlog.ui.GameStatePanel;
import com.evansloan.collectionlog.ui.Icon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EnumSet;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class CollectionLogPanel extends PluginPanel
{
	private static final ImageIcon ACCOUNT_ICON;
	private static final ImageIcon DISCORD_ICON;
	private static final ImageIcon GITHUB_ICON;
	private static final ImageIcon HELP_ICON;
	private static final ImageIcon INFO_ICON;
	private static final ImageIcon WEBSITE_ICON;
	private static final EmptyBorder DEFAULT_BORDER = new EmptyBorder(10, 10, 10, 10);

	static
	{
		ACCOUNT_ICON = Icon.ACCOUNT.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		DISCORD_ICON = Icon.DISCORD.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		GITHUB_ICON = Icon.GITHUB.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
		HELP_ICON = Icon.HELP.getIcon(img -> ImageUtil.resizeImage(img, 20, 20));
		INFO_ICON = Icon.INFO.getIcon(img -> ImageUtil.resizeImage(img, 20, 20));
		WEBSITE_ICON = Icon.COLLECTION_LOG.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
	}

	private static GameState currentGameState;

	private final CollectionLogPlugin collectionLogPlugin;
	private final CollectionLogManager collectionLogManager;
	private final ClientThread clientThread;
	private final CollectionLogConfig config;

	private GameStatePanel accountPanel;
	private JLabel clnEnabledLabel;
	private JButton uploadCollectionLogBtn;
	private JButton deleteCollectionLogBtn;
	private JTextArea statusTextArea;
	private JPanel accountSettingsPanel;
	private JComboBox<DisplayRankType> displayRankComboBox;
	private JCheckBox showQuantityCheck;

	public CollectionLogPanel(
		CollectionLogPlugin collectionLogPlugin,
		CollectionLogManager collectionLogManager,
		ClientThread clientThread,
		CollectionLogConfig config
	)
	{
		super(false);

		this.collectionLogPlugin = collectionLogPlugin;
		this.collectionLogManager = collectionLogManager;
		this.clientThread = clientThread;
		this.config = config;
	}

	public void create(GameState gameState)
	{
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());
		setBorder(DEFAULT_BORDER);

		JPanel layoutPanel = new JPanel();
		layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));

		JPanel titlePanel = createTitlePanel();
		JPanel tabPanel = createTabPanel(gameState);

		layoutPanel.add(titlePanel);
		layoutPanel.add(tabPanel);

		add(layoutPanel, BorderLayout.NORTH);
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

	private JPanel createTabPanel(GameState gameState)
	{
		boolean isLoggedIn = gameState == GameState.LOGGED_IN;

		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));

		JPanel activeTabPanel = new JPanel();
		activeTabPanel.setLayout(new BoxLayout(activeTabPanel, BoxLayout.Y_AXIS));
		MaterialTabGroup tabGroup = new MaterialTabGroup(activeTabPanel);
		tabGroup.setLayout(new GridLayout(1, 3, 10, 10));
		tabGroup.setBorder(new EmptyBorder(0, 0, 10, 0));
		tabPanel.add(tabGroup);
		tabPanel.add(activeTabPanel);

		JPanel infoPanel = createInfoPanel();
		activeTabPanel.add(infoPanel);
		createTab(INFO_ICON, "Info", tabGroup, infoPanel).select();

		accountPanel = createAccountPanel(isLoggedIn);
		createTab(ACCOUNT_ICON, "Account", tabGroup, accountPanel);

		JPanel helpPanel = createHelpPanel();
		createTab(HELP_ICON, "Help", tabGroup, helpPanel);

		return tabPanel;
	}

	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

		JPanel pluginInfoPanel = new JPanel(new GridLayout(0, 1));
		pluginInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		pluginInfoPanel.setBorder(DEFAULT_BORDER);
		infoPanel.add(pluginInfoPanel);

		JLabel versionLabel = createKeyValueLabel("Version: ", CollectionLogConfig.PLUGIN_VERSION);
		clnEnabledLabel = createKeyValueLabel(
			"collectionlog.net uploads: ",
			config.allowApiConnections() ? "Enabled" : "Disabled"
		);
		pluginInfoPanel.add(versionLabel);
		pluginInfoPanel.add(clnEnabledLabel);

		JPanel changeLogContent = createTabContentPanel();
		String changeLogText = "v" + CollectionLogConfig.PLUGIN_VERSION + "\n\n" +
			"* Side panel redesign\n" +
			"  - Add quick start guide\n" +
			"  - Add collectionlog.net account settings tab\n" +
			"  - Add FAQ tab to help with the most common questions/issues\n\n" +
			"* Add config option to customize highlight colors for incomplete pages";
		JTextArea changeLogTextArea = createTextArea(changeLogText);
		changeLogContent.add(changeLogTextArea);
		ExpandablePanel changeLogPanel = new ExpandablePanel("What's new", changeLogContent);
		infoPanel.add(changeLogPanel);

		JPanel quickStartContent = createTabContentPanel();
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

		return infoPanel;
	}

	private GameStatePanel createAccountPanel(boolean isLoggedIn)
	{
		GameStatePanel gameStatePanel = new GameStatePanel();
		gameStatePanel.setLayout(new BoxLayout(gameStatePanel, BoxLayout.Y_AXIS));

		// Account tab logged out state
		JLabel accountLoggedOutLabel = new JLabel("Log in to manage your account");
		accountLoggedOutLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		gameStatePanel.setLoggedOutContent(accountLoggedOutLabel);

		// Account tab logged in state
		JPanel accountLoggedInPanel = new JPanel();
		accountLoggedInPanel.setLayout(new BorderLayout());
		gameStatePanel.setLoggedInContent(accountLoggedInPanel);

		statusTextArea = createTextArea("");
		accountLoggedInPanel.add(statusTextArea, BorderLayout.NORTH);

		JPanel accountButtonPanel = createAccountButtonPanel();
		accountLoggedInPanel.add(accountButtonPanel, BorderLayout.CENTER);

		accountSettingsPanel = createAccountSettingsPanel();
		accountSettingsPanel.setVisible(config.allowApiConnections());
		accountLoggedInPanel.add(accountSettingsPanel, BorderLayout.SOUTH);

		gameStatePanel.updateContent(isLoggedIn);

		return gameStatePanel;
	}

	private JPanel createAccountSettingsPanel()
	{
		JPanel settingsPanel = new JPanel(new BorderLayout());
		settingsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel settingsLabel = createTitleLabel("Account settings");
		settingsPanel.add(settingsLabel, BorderLayout.NORTH);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		JPanel showQuantityPanel = new JPanel(new BorderLayout());
		showQuantityPanel.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

		JLabel showQuantityLabel = new JLabel("Show item quantities");
		showQuantityLabel.setForeground(Color.WHITE);
		showQuantityLabel.setToolTipText("Toggle the display of item quantities on collectionlog.net");
		showQuantityPanel.add(showQuantityLabel, BorderLayout.CENTER);

		showQuantityCheck = createCheckBox((event) -> {
			collectionLogManager.getUserSettings()
				.setShowQuantity(event.getStateChange() == ItemEvent.SELECTED);
		});
		showQuantityPanel.add(showQuantityCheck, BorderLayout.EAST);

		controlPanel.add(showQuantityPanel);

		JPanel displayRankPanel = new JPanel(new BorderLayout());
		displayRankPanel.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

		JLabel displayRankLabel = new JLabel("Display rank");
		displayRankLabel.setForeground(Color.WHITE);
		displayRankLabel.setToolTipText("Rank type to display on collectionlog.net profile");
		displayRankPanel.add(displayRankLabel, BorderLayout.CENTER);

		displayRankComboBox = createComboBox(DisplayRankType.values(), event -> {
			collectionLogManager.getUserSettings().setDisplayRank((DisplayRankType) event.getItem());
		});
		displayRankPanel.add(displayRankComboBox, BorderLayout.EAST);

		controlPanel.add(displayRankPanel);
		settingsPanel.add(controlPanel, BorderLayout.CENTER);

		return settingsPanel;
	}

	private JPanel createAccountButtonPanel()
	{
		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel titleLabel = createTitleLabel("Account management");
		buttonPanel.add(titleLabel);

		uploadCollectionLogBtn = createButton(
			"Upload collection log",
			(event) -> {
				collectionLogPlugin.setCollectionLogDeleted(false);
				setStatus("Uploading collection log to collectionlog.net...", false, false);
				clientThread.invokeLater(collectionLogPlugin::saveCollectionLogData);
			}
		);
		uploadCollectionLogBtn.setVisible(config.allowApiConnections());
		buttonPanel.add(uploadCollectionLogBtn);

		JButton clearCollectionLogBtn = createButton(
			"Reset collection log data",
			(event) -> collectionLogManager.deleteSaveFile()
		);
		buttonPanel.add(clearCollectionLogBtn);

		deleteCollectionLogBtn = createButton(
			"Delete collection log",
			(event) -> {
				int confirm = JOptionPane.showOptionDialog(
					deleteCollectionLogBtn,
					"Are you sure you want to delete your collection log from collectionlog.net?\n" +
					"Make sure to disable \"Allow collectionlog.net connections\" config to prevent future uploads.",
					"Delete collection log?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					new String[]{"Yes", "No"},
					"No"
				);

				if (confirm == JOptionPane.YES_OPTION)
				{
					setStatus("Deleting collection log from collectionlog.net...", false, false);
					clientThread.invokeLater(collectionLogPlugin::deleteCollectionLog);
				}
			}
		);
		deleteCollectionLogBtn.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);
		deleteCollectionLogBtn.setVisible(config.allowApiConnections());
		buttonPanel.add(deleteCollectionLogBtn);

		return buttonPanel;
	}

	private JPanel createHelpPanel()
	{
		JPanel helpPanel = new JPanel();
		helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));

		JPanel notUploadingContent = createTabContentPanel();
		String notUploadingText = "If your collection log is not appearing on collectionlog.net, follow these steps:\n\n" +
			"1. Make sure the \"Allow collectionlog.net connections\" config is enabled.\n\n" +
			"2. Click through every page in the collection log to gather your obtained item data.\n\n" +
			"3. Click the \"Upload collection log\" button in the account tab or log out to upload your collection log.\n\n" +
			"4. If the above steps do not solve your problem, click the \"Reset collection log\" button in the accounts tab " +
			"and try the above steps again.";
		JTextArea notUploadingTextArea = createTextArea(notUploadingText);
		notUploadingContent.add(notUploadingTextArea);
		ExpandablePanel notUploadingPanel = new ExpandablePanel("Collection log not uploading?", notUploadingContent);
		helpPanel.add(notUploadingPanel);

		JPanel missingItemsContent = createTabContentPanel();
		String missingItemsText = "1. Click through any page in the collection log you may have received a new item in.\n\n" +
			"2. Click the \"Upload collection log\" button in the account tab or log out to upload your collection log.";
		JTextArea missingItemsTextArea = createTextArea(missingItemsText);
		missingItemsContent.add(missingItemsTextArea);
		ExpandablePanel missingItemsPanel = new ExpandablePanel("Missing obtained items?", missingItemsContent);
		helpPanel.add(missingItemsPanel);

		JPanel duplicateItemsContent = createTabContentPanel();
		String duplicateItemsText = "If your collectionlog.net collection log page is showing dupliate items/kill counts, join the " +
			"Log Hunters Discord server by clicking on the Discord icon above and reporting a bug in the bugs-and-support forum with the \"Plug-in\" tag";
		JTextArea duplicateItemsTextArea = createTextArea(duplicateItemsText);
		duplicateItemsContent.add(duplicateItemsTextArea);
		ExpandablePanel duplicateItemsPanel = new ExpandablePanel("Duplicate items?", duplicateItemsContent);
		helpPanel.add(duplicateItemsPanel);

		JPanel suggestionContent = createTabContentPanel();
		String suggestionText = "If you have a suggestion for the collection log plugin or collectionlog.net, feel free to post " +
			"it in the Log Hunters Discord server in the suggestions forum";
		JTextArea suggestionTextArea = createTextArea(suggestionText);
		suggestionContent.add(suggestionTextArea);
		ExpandablePanel suggestionPanel = new ExpandablePanel("Have a suggestion?", suggestionContent);
		helpPanel.add(suggestionPanel);

		return helpPanel;
	}

	private JTextArea createTextArea(String text)
	{
		JTextArea textArea = new JTextArea(0, 22);
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
		btn.setBorder(new EmptyBorder(15, 0, 15, 0));
		btn.setText(text);
		btn.setForeground(Color.WHITE);
		btn.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 5, 30));
		btn.addActionListener(actionListener);
		btn.setFocusable(false);

		return btn;
	}

	private MaterialTab createTab(ImageIcon icon, String toolTipText, MaterialTabGroup tabGroup, JComponent content)
	{
		MaterialTab tab = new MaterialTab(icon, tabGroup, content);
		tab.setToolTipText(toolTipText);
		tabGroup.addTab(tab);

		return tab;
	}

	private JPanel createTabContentPanel()
	{
		JPanel tabContentPanel = new JPanel(new GridLayout(0, 1));
		tabContentPanel.setBorder(DEFAULT_BORDER);
		tabContentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		return tabContentPanel;
	}

	private String htmlText(String text)
	{
		return "<html><body style='color:#A5A5A5'>" + text + "</body></html>";
	}

	private String getKeyValueText(String key, String value)
	{
		return htmlText(key + "<span style='color:white'>" + value + "</span>");
	}

	private JLabel createKeyValueLabel(String key, String value)
	{
		return new JLabel(getKeyValueText(key, value));
	}

	private JLabel createTitleLabel(String text)
	{
		JLabel titleLabel = new JLabel(text);
		titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);

		return titleLabel;
	}

	private JCheckBox createCheckBox(ItemListener itemListener)
	{
		JCheckBox checkBox = new JCheckBox();
		checkBox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		checkBox.addItemListener(itemListener);
		checkBox.setSelected(true);

		return checkBox;
	}

	private <T extends Enum<T>> JComboBox<T> createComboBox(T[] items, ItemListener itemListener)
	{
		JComboBox<T> comboBox = new JComboBox<>(items);
		comboBox.setRenderer(new ComboBoxListRenderer<>());
		comboBox.setForeground(Color.WHITE);
		comboBox.setFocusable(false);
		comboBox.addItemListener(itemListener);

		return comboBox;
	}

	public void setUserSettings(UserSettings userSettings)
	{
		displayRankComboBox.setSelectedItem(userSettings.getDisplayRank());
		showQuantityCheck.setSelected(userSettings.isShowQuantity());
	}

	/**
	 * Set the status message that appears at the top of the account panel and
	 * enables/disables upload and delete buttons
	 *
	 * @param message Message to display. Pass null to keep previous message
	 * @param isError Set to true to color message text red
	 * @param enableBtn Should upload/delete buttons be enabled
	 */
	public void setStatus(String message, boolean isError, boolean enableBtn)
	{
		Color textColor = Color.WHITE;
		if (isError)
		{
			textColor = ColorScheme.PROGRESS_ERROR_COLOR;
		}

		if (message != null)
		{
			statusTextArea.setText(message);
			statusTextArea.setForeground(textColor);
		}

		uploadCollectionLogBtn.setEnabled(enableBtn);
		deleteCollectionLogBtn.setEnabled(enableBtn);
	}

	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getKey().equals("upload_collection_log"))
		{
			clnEnabledLabel.setText(getKeyValueText(
				"collectionlog.net uploads: ",
				config.allowApiConnections() ? "Enabled" : "Disabled"
			));

			uploadCollectionLogBtn.setVisible(config.allowApiConnections());
			deleteCollectionLogBtn.setVisible(config.allowApiConnections());
			accountSettingsPanel.setVisible(config.allowApiConnections());

			accountPanel.revalidate();
			accountPanel.repaint();
		}
	}

	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState != GameState.LOGGED_IN && gameState != GameState.LOGIN_SCREEN)
		{
			return;
		}

		// Check if game state has changed to prevent updating panels
		// when going from LOGGED_IN -> LOADING -> LOGGED_IN
		boolean isNewGameState = gameState != currentGameState;
		if (!isNewGameState)
		{
			return;
		}
		currentGameState = gameState;

		GameStatePanel.updatePanels(gameState);

		if (gameState == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() -> {
				Client client = collectionLogPlugin.getClient();
				EnumSet<DisplayRankType> displayRankTypes = DisplayRankType.getRankTypesFromAccountType(client.getAccountType());

				displayRankComboBox.removeAllItems();

				for (DisplayRankType displayRankType : displayRankTypes)
				{
					displayRankComboBox.addItem(displayRankType);
				}
			});
		}
	}
}
