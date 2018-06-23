package net.simplyrin.tweetcore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import net.simplyrin.jsonloader.JsonLoader;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Created by SimplyRin on 2018/06/23.
 *
 * Copyright (C) 2018 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Main {

	private static Twitter twitter;

	public static void main(String[] args) {
		Main.println("----------------------------------------------------------------");
		Main.println("ソースコード: https://github.com/SimplyRin/TweetCore");
		Main.println("ライセンス: GPLv3 (https://github.com/SimplyRin/TweetCore/blob/master/LICENSE.md)");
		Main.println("----------------------------------------------------------------");

		try {
			Thread.sleep(500);
			Main.println(" ");
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}



		Main.println("読み込み中...。");

		File config = new File("config.json");
		if(!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Scanner scanner = new Scanner(System.in);
			Main.print("初期設定を行いますか？ (Yes/No): ");
			if(scanner.nextLine().equalsIgnoreCase("yes")) {
				Main.init();
			}
		}

		JsonLoader jsonLoader = JsonLoader.getJson(config);

		twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(jsonLoader.getString("Consumer-Key"), jsonLoader.getString("Consumer-Secret"));
		twitter.setOAuthAccessToken(new AccessToken(jsonLoader.getString("Access-Token"), jsonLoader.getString("Access-Token-Secret")));

		try {
			User user = twitter.verifyCredentials();

			Main.println("ユーザー名: @" +  user.getScreenName());
		} catch (TwitterException e) {
			Main.failed(e);
			return;
		}

		Main.println("読み込みが完了しました。");
		Main.println("/help でコマンド一覧を確認できます。");

		while(true) {
			Main.task();
		}
	}

	private static void task() {
		Scanner scanner = new Scanner(System.in);

		String[] args = scanner.nextLine().split(" ");

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("/help")) {
				Main.println("/tweet <内容> : 入力した内容をツイートします。");
				Main.println("/reply <ツイートID> <内容> : ツイートに返信します。");
				Main.println("/reset : 初期化し、再設定を開始します。");
				Main.println("/quit : プログラムを停止します。");
				return;
			}

			if(args[0].equalsIgnoreCase("/tweet")) {
				if(args.length > 1) {
					String message = "";
					for(int i = 1; i < args.length; i++) {
						message = message + args[i] + " ";
					}
					try {
						twitter.updateStatus(message);
						Main.println("ツイートしました！ : " + message);
					} catch (TwitterException e) {
						Main.failed(e);
						return;
					}
					return;
				}
				Main.println("/tweet <内容> : 入力した内容をツイートします。");
				return;
			}

			if(args[0].equalsIgnoreCase("/reply")) {
				if(args.length > 2) {
					try {
						Status status = twitter.showStatus(Long.valueOf(args[1]));
						String message = "";
						for(int i = 2; i < args.length; i++) {
							message = message + args[i] + " ";
						}
						twitter.updateStatus(new StatusUpdate("@" + status.getUser().getScreenName() + " " + message).inReplyToStatusId(status.getId()));
						Main.println("@" + status.getUser().getScreenName() + " のツイートに返信しました: " + message);
						return;
					} catch (Exception e) {
						Main.failed(e);
						return;
					}
				}
				Main.println("/reply <ツイートID> <内容> : ツイートに返信します。");
				return;
			}

			if(args[0].equalsIgnoreCase("/reset")) {
				if(args.length > 1) {
					if(args[1].equalsIgnoreCase("yes")) {
						Main.init();
						return;
					}
				}
				Main.println("再設定するには /tweet yes と入力してください。");
				return;
			}

			if(args[0].equalsIgnoreCase("/quit") || args[0].equalsIgnoreCase("/exit")) {
				Main.println("ばーい^^");
				System.exit(0);
				return;
			}
		}

		Main.println("不明なコマンドです。 /help でコマンド一覧を確認できます。");
	}

	private static void init() {
		Scanner scanner = new Scanner(System.in);

		JsonLoader jsonLoader = new JsonLoader();
		jsonLoader.put("Consumer-Key", "KEY");
		jsonLoader.put("Consumer-Secret", "SECRET");
		jsonLoader.put("Access-Token", "TOKEN");
		jsonLoader.put("Access-Token-Secret", "TOKEN_SECRET");
		JsonLoader.saveJson(jsonLoader, "config.json");

		Main.print("Consumer Key を入力してください: ");
		jsonLoader.put("Consumer-Key", scanner.nextLine());

		Main.print("Consumer Secret を入力してください: ");
		jsonLoader.put("Consumer-Secret", scanner.nextLine());

		Main.print("Access Token を入力してください: ");
		jsonLoader.put("Access-Token", scanner.nextLine());

		Main.print("Access Token Secret を入力してください: ");
		jsonLoader.put("Access-Token-Secret", scanner.nextLine());

		JsonLoader.saveJson(jsonLoader, "config.json");
	}

	private static void failed(Exception e) {
		Main.println("ツイートに失敗しました。エラー内容:");
		e.printStackTrace();
		System.exit(0);
	}

	private static void println(String message) {
		System.out.println(Main.getTimePrefix() + message);
	}

	private static void print(String message) {
		System.out.print(Main.getTimePrefix() + message);
	}

	private static String getTimePrefix() {
		Date data = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return "[" + sdf.format(data) + "] ";
	}

}
