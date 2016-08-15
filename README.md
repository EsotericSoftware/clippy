# Clippy

Clippy is a small, multifunctional Windows productivity tool for programmers and other power users. Clippy runs in the background and provides a powerful clipboard history, easy uploading of screenshots, files, and text, and optional features to improve your health when using a computer for long periods of time.

* **Clipboard history** Clippy's main purpose is to store in a database any text items that are copied to the Windows clipboard, which can later be easily retrieved. The usefulness of this for programming cannot be understated!

* **Screenshots** Clippy can take screenshots and upload them, as well as easily upload files, zips, and text/pastes. Uploading can be done via FTP, SFTP, [imgur.com](http://imgur.com/), and/or [pastebin.com](http://pastebin.com/).

* **Break warnings** Clippy can display a gentle reminder when you have been at the computer too long. This helps to avoid the unhealthy effects of being sedentary. Damage done by sitting for long periods is not undone by being active at other times. It's suggested to stand up for 5 minutes every hour.

* **Blue light filter** Clippy can filter the blue light emitted by your monitor based on the time of day. This is useful to reduce your exposure to blue light in the evening, which can otherwise interfere with your sleep schedule, [melatonin](https://en.wikipedia.org/wiki/Melatonin) production, and [circadian rhythm](https://en.wikipedia.org/wiki/Circadian_rhythm), potentially leading to [delayed sleep phase disorder](https://en.wikipedia.org/wiki/Delayed_sleep_phase_disorder).

* **Philips Hue** Clippy can control [Philips Hue](http://meethue.com) lights based on the time of day. Similar to blue light filtering for your monitor, this can help your body recognize when it is evening time and improve both your sleep schedule and quality of sleep.

## Features and uses

* It is very common to copy multiple pieces of text. Since the Windows clipboard only holds a single item, without Clippy this means finding and copying the same text many times.

* Clippy can be used simply to keep text safe, in case it is needed later. While writing an email or before refactoring code, you can select the text and copy it, knowing that Clippy has stored it should you ever need it again in the future.

* Previously copied text can be browsed by pressing the history popup hotkey. Search the clipboard history by simply typing any portion of the text. Any text you've ever copied can be easily found later, which can be useful for phone numbers, passwords, etc. Clippy's database can efficiently store millions of clips.

* Use a hotkey to take a screenshot of the whole screen, the foreground application, or draw a rectangle with the mouse. Clippy will upload the image and paste a URL to it.

* Text pasted from Clippy's popup is always plain text. This means Clippy can be used to quickly strip unwanted formatting from text.

* When files are copied, Clippy stores the file paths as text. This makes it easy to copy a file and paste its path to the command line, a file dialog, or elsewhere.

* Uploading and sharing files is as easy as copying, then pressing the upload hotkey.

* Losing track of time can mean sitting at the computer for many hours without noticing. Clippy provides a reminder of how long you have been using the computer without a break, helping to avoid abuse of your body.

* Working in front of a bright screen at night can interfere with your sleep schedule. In the evening hours, Clippy can reduce both your monitor's brightness and the lighting in your home or office, reducing blue light which would otherwise prevent melatonin production and make going to sleep more difficult.

* Clippy is designed to make common tasks very easy, without the bloat and clutter of unnecessary features.

## Download

Download the latest version of Clippy here:

[Download Clippy](https://github.com/EsotericSoftware/clippy/releases)

There is no installation, instead Clippy is provided as two files. `Clippy.exe` is an executable that automatically finds Java and runs Clippy. Alternatively, `Clippy.jar` can be run directly either by double clicking or with `javaw -jar Clippy.jar`. Both files are not needed, each file is the whole app.

It is highly recommended to run Clippy using 64-bit Java. Clippy may run much more slowly if 32-bit Java is used. To be sure of which Java installation Clippy uses, run it from the command line using the:

```
C:\path\to\64-bit\Java\javaw -jar C:\path\to\Clippy.jar
```

Clippy runs in the background as an icon in the system tray. It stores any text items that are placed on the clipboard and waits for hotkeys to show the clipboard history popup and take other actions. Clippy can be exited by clicking the icon in the system tray.

## Popup

![](http://i.imgur.com/WLVBl8g.gif)

The default hotkey to show the popup is `ctrl+shift+insert`. The popup shows the most recent clipboard items and you may begin typing to search the entire history. The following keys are available on the popup:
- `up` or `down` selects an item.
- `enter` puts the selected item on the clipboard, hides the popup, and pastes it.
- `home` or `end` selects the first or last item.
- `page up` or `page down` shows additional pages of items.
- `escape` or clicking outside the popup hides the popup.
- `ctrl+shift+delete` deletes the selected item.
- `ctrl+shift+enter` uploads the item as text and puts a link on the clipboard.

### Search

The entire search history can be searched simply by typing while the popup is open. Items containing any of the typed text are shown, sorted be most recently used first. `%` can be used as a wildcard. Use `\%` to search for a percent sign.

### Menu

A menu is shown on the popup by pressing `alt`. Once shown, the numbers `0-9` can be pressed to choose an item. Pressing `alt` plus a number can be used as a shortcut. For example, `alt+2` will paste the second item in the list.

The `Lock items` checkbox shown at the bottom of the popup when `alt` is pressed prevents the order of items from changing when an existing item is chosen. This can be useful when pasting a number of different entries using number keys.

## Screenshots

The default hotkey to take a region screenshot is `ctrl+alt+\`. Once activated, click and drag to specify the rectangular region to screenshot. The resulting image is [uploaded](#uploading) and a link is placed on the clipboard when the upload is complete. Pressing `ctrl+shift+\` repeats the last region screenshot. The default hotkey to screenshot the foreground window is `ctrl+alt+shift+\`. The hotkey to screenshot the whole window is not mapped by default.

![](http://i.imgur.com/1K00xlY.gif)

While taking a region screenshot, hold `shift` to move more slowly and `ctrl` to lock to the X or Y axis.

## Configuration

Clippy stores its configuration file and the database in user folder, under the `.clippy` subfolder. The `config.json` file can be edited to configure Clippy. For example, you can customize the popup and other hotkeys, the number of items shown on the popup, and more.

Hotkeys are described using [these constants](https://docs.oracle.com/javase/8/docs/api/java/awt/event/KeyEvent.html) with the "VK_" prefix omitted.

### Uploading

Clippy has 3 settings for uploading:

* Text can be uploaded to FTP, SFTP, or [pastebin.com](http://pastebin.com/).
* Images can be uploaded to FTP, SFTP, or [imgur.com](http://imgur.com/).
* Files can be uploaded to FTP or SFTP.

The default settings use pastebin for text, imagur for images, and disallow file upload. FTP and SFTP must be configured in the `config.json` file before use.

### Blue light reduction

Clippy can adjust the Windows gamma based on the time of day. Gamma controls the amount of red, green, and blue displayed on the screen. The gamma changes are defined as a timeline:

```
gamma: [
	{ time: 6:00am, brightness: 1, r: 1, g: 1, b: 1 }
	{ time: 6:00pm, brightness: 1, r: 1, g: 1, b: 1 }
	{ time: 9:00pm, brightness: 1, r: 1, g: 0.7, b: 0.6 }
	{ time: 5:30am, brightness: 1, r: 1, g: 0.7, b: 0.6 }
]
```

The r, g, and b values are percentages where 0 means none of that color and 1 means the normal amount, before Clippy's changes. The r, g, and b values are multiplied by the brightness. The percentages are smoothly interpolated between the times. The timeline above works as follows:

```
6:00am to 6:00pm: transition from 1, 1, 1, 1 to 1, 1, 1, 1 (no change)
6:00pm to 9:00pm: transition from 1, 1, 1, 1 to 1, 1, 0.7, 0.6
9:00pm to 5:30am: transition from 1, 1, 0.7, 0.6 to 1, 1, 0.7, 0.6 (no change)
5:30am to 6:00am: transition from 1, 1, 0.7, 0.6 to 1, 1, 1, 1
```

#### Gamma limits

By default, Windows limits the range of gamma values from 0.5 to 1. This is a safeguard against software setting the gamma to 0 so you only see a black screen. To remove these limits, apply this [registry file](https://github.com/EsotericSoftware/clippy/blob/master/build/gamma.reg) and reboot. Clippy still prevents gamma from being set so low that the screen is completely black.

#### Gamma alternatives

Other software such as [f.lux](http://justgetflux.com/) or [Sunset Screen](http://www.skytopia.com/software/sunsetscreen/) can also adjust the gamma, but they provide only day and night settings while Clippy's timeline allows for any number of transitions. Also, Clippy allows the red, green, and blue amounts to be set separately. By reducing the green slightly less than the blue, the red tint on the screen will be much less noticeable.

### Philips Hue

Clippy can connect to a Philips Hue bridge and adjust the color of any number of lights based on the time of day. An example configuration is as follows:

```
philipsHueEnabled: true
philipsHueIP: 192.168.0.12:80
philipsHueUser: 882c06f1e832a58f4a4e2a09b0b5bc2
philipsHue: [
	{
		name: 1
		model: null
		timeline: [
			{ time: 5:00am, brightness: 1, r: 1, g: 1, b: 1 }
			{ time: 7:00pm, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
			{ time: 4:30am, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
		]
	}
	{
		name: group:2
		model: LCT001
		timeline: [
			{ time: 1:00pm, brightness: 1, r: 1, g: 1, b: 1 }
			{ time: 2:00pm, brightness: 1, r: 0.6, g: 0, b: 0 }
			{ time: 3:00pm, brightness: 1, r: 1, g: 1, b: 1 }
		]
	}
]
```

`philipsHueEnabled` must be set to true to enable connecting to the Philips Hue bridge. Most users won't configure `philipsHueIP` and `philipsHueUser` manually. Instead, leave them set to `null` and Clippy will search for your Philips Hue bridge and configure the two settings automatically. The `philipsHue` setting is a list of lights to change, each with their own timeline which works in the same way as the [gamma](#blue-light-reduction) setting.

If `name` is `null`, the section will control all lights. If `name` begins with `group:` then the section will control that group, for example `group:office` would control the group named `office`. Otherwise `name` is the ID of a single light which will be controlled by the section.

Each Philips Hue light model supports a specific [color gamut](http://www.developers.meethue.com/documentation/supported-lights). When setting the color of all lights or for a group, the `model` setting is used to convert the r, g, b into a color the specified light model can use. When setting the color of a single light, the light's actual model is used and the `model` setting is ignored.

## Database

The clipboard history is stored in a relational H2 database which can be opened using the H2 console:

```
java -cp Clippy.jar org.h2.tools.Console
```

This will open a browser to the H2 console interface. To connect to your Clippy database, set the JDBC URL to `jdbc:h2:file:~/.clippy/db/db` and the username and password to blank. You may then execute SQL queries, for example:

```
SELECT COUNT(*) FROM clips; -- Get total number of clips.
SELECT MAX(id) FROM clips; -- Get highest clip ID.
DELETE FROM clips WHERE id < 10000; -- Delete some old clips.
```

## Development details

Clippy's codebase is clean and straightforward. It is written in Java and uses [JNA](https://github.com/twall/jna/) to access the Windows APIs necessary to monitor and interact with the [clipboard](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Clipboard.java), system-wide [hotkeys](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Keyboard.java), the [system tray](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Tray.java), and more.

The clipboard history is stored in an [H2](http://www.h2database.com) relational database. A small [data store](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/ClipDataStore.java) API simplifies interaction with the database.

While Clippy is written in Java and most of it is platform agnostic, a number of Windows specific APIs are needed: accessing the clipboard events and contents, global hotkeys, showing the history popup at the cursor location, and more. These could be abstracted so Clippy can be ported to other languages, but since Clippy's author uses Windows there is not much motivation for that effort.

## License

Clippy is released as OSS under the [New BSD license](https://github.com/EsotericSoftware/clippy/blob/master/LICENSE).
