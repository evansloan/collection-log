package com.evansloan.collectionlog.ui;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameState;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameStatePanel extends JPanel
{
	private static List<GameStatePanel> gameStatePanelList = new ArrayList<>();

	private JComponent loggedInContent;
	private JComponent loggedOutContent;

	public GameStatePanel()
	{
		gameStatePanelList.add(this);
	}

	public static void updatePanels(GameState gameState)
	{
		boolean isLoggedIn = gameState == GameState.LOGGED_IN;

		for (GameStatePanel gameStatePanel : gameStatePanelList)
		{
			gameStatePanel.updateContent(isLoggedIn);
		}
	}

	public void updateContent(boolean isLoggedIn)
	{
		removeAll();

		if (isLoggedIn)
		{
			add(loggedInContent);
		}
		else
		{
			add(loggedOutContent);
		}

		revalidate();
		repaint();
	}
}
