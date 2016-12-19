# Clippy

Clippy is a small, multifunctional Windows productivity tool for programmers and other power users. Clippy runs in the background and provides a powerful clipboard history, easy uploading of screenshots, files, and text, and optional features to improve your health when using a computer for long periods of time.

* **Clipboard history** Clippy's main purpose is to store in a database any text items that are copied to the Windows clipboard, which can later be easily retrieved. The usefulness of this for programming cannot be understated!

* **Screenshots** Clippy can take screenshots and upload them, as well as easily upload files, zips, and text/pastes. Uploading can be done via FTP, SFTP, [imgur.com](http://imgur.com/), and/or [pastebin.com](http://pastebin.com/).

* **Break warnings** Clippy can display a gentle reminder when you have been at the computer too long. This helps to avoid the unhealthy effects of being sedentary. Damage done by sitting for long periods is not undone by being active at other times. It's suggested to stand up for 5 minutes every hour.

* **Blue light filter** Clippy can filter the blue light emitted by your monitor based on the time of day. This is useful to reduce your exposure to blue light in the evening, which can otherwise interfere with your sleep schedule, [melatonin](https://en.wikipedia.org/wiki/Melatonin) production, and [circadian rhythm](https://en.wikipedia.org/wiki/Circadian_rhythm), potentially leading to [delayed sleep phase disorder](https://en.wikipedia.org/wiki/Delayed_sleep_phase_disorder).

* **Eye tracking** Clippy can use a [Tobii Eye Tracker 4C](https://tobiigaming.com/eye-tracker-4c/) to control the mouse. Just look at the screen where you want to click and press a hotkey.

* **Lighting control** Clippy can control [Philips Hue](http://meethue.com) lights based on the time of day. Similar to blue light filtering for your monitor, this can help your body recognize when it is evening time and improve both your sleep schedule and quality of sleep.

* **Dyanmic DNS** Clippy can keep your IP in sync with [DnsMadeEasy](http://dnsmadeeasy.com) so you can always access your computer.

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

## Uploading

Clippy has 3 settings for uploading:

* Text can be uploaded to FTP, SFTP, or [pastebin.com](http://pastebin.com/).
* Images can be uploaded to FTP, SFTP, or [imgur.com](http://imgur.com/).
* Files can be uploaded to FTP or SFTP.

The default settings use pastebin for text, imagur for images, and disallow file upload. FTP and SFTP must be configured in the `config.json` file before use.

## Blue light reduction

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

### Colors

Colors can be described as RGB values, as shown above, or as a color temperature:

```
gamma: [
	{ time: 6:00am, brightness: 1, temp: 6500 }
	{ time: 6:00pm, brightness: 1, temp: 6500 }
	{ time: 9:00pm, brightness: 0.8, temp: 3800 }
	{ time: 5:30am, brightness: 0.8, temp: 3800 }
]
```

### Times

Times can be described using 12 hour time, as shown above, or 24 hour time:

```
gamma: [
	{ time: 06:00, brightness: 1, temp: 6500 }
	{ time: 18:00, brightness: 1, temp: 6500 }
	{ time: 21:00, brightness: 0.8, temp: 3800 }
	{ time: 05:30, brightness: 0.8, temp: 3800 }
]
```

Time can also be describe using `sunrise:xxx` or `sunset:xxx` where `xxx` is your latitude. Time can be relative to the sunrise or sunset by using `sunset+12:xxx` or `sunset-12:xxx` where `12` is the number of minutes to add or subtract.

```
gamma: [
	{ time: sunrise:43.4357, brightness: 1, temp: 6500 }
	{ time: sunset-60:43.4357, brightness: 1, temp: 6500 }
	{ time: sunset+120:43.4357, brightness: 0.8, temp: 3800 }
	{ time: sunrise-30:43.4357, brightness: 0.8, temp: 3800 }
]
```

### Gamma toggle

The `toggleHotkey` toggles the gamma between Clippy's gamma and 100%. This can be useful when watching videos or doing tasks that require seeing correct colors.

```
toggleHotkey: ctrl shift alt Z
```

### Gamma reset

Some applications may interfere with the Windows gamma. For example, some OpenGL applications reset the gamma to 100% when they close. This is relatively rare, but if this happens, clicking the Clippy icon in the system tray will reapply Clippy's gamma setting. You could also use the toggle hotkey twice to toggle gamma off and then back on.

### Gamma limits

By default, Windows limits the range of gamma values from 0.5 to 1. This is a safeguard against software setting the gamma to 0 so you only see a black screen. To remove these limits, apply this [registry file](https://github.com/EsotericSoftware/clippy/blob/master/build/gamma.reg) and reboot. Clippy still prevents gamma from being set so low that the screen is completely black.

### Gamma alternatives

Other software such as [f.lux](http://justgetflux.com/) or [Sunset Screen](http://www.skytopia.com/software/sunsetscreen/) can also adjust the gamma, but they provide only day and night settings while Clippy's timeline allows for any number of transitions. Also, Clippy allows the red, green, and blue amounts to be set separately. By reducing the green slightly less than the blue, the red tint on the screen will be much less noticeable.

## Break warnings

Research shows that sitting for long periods of time is detrimental to your health and is not mitigated by extended activity at other times. Clippy can monitor keyboard and mouse access and provide a warning when the computer has been used without a break for too long. A popup is shown in the bottom right corner of the screen which displays the number of minutes without a break. Set `breakWarningMinutes` to zero to disable. `breakResetMinutes` sets the duration of the break, after which the popup is hidden.

```
breakWarningMinutes: 55
breakResetMinutes: 5
```

### Warning sounds

By default Clippy plays an obnoxious sound when a break needs to be taken and another periodically if the break warning is ignored. This can help you to remember to take a break, as it soon becomes easy to ignore the warning popup. The warning sound is played periodically at increasing volume if a break is not taken. The default settings use the default sounds, but the sounds can be changed by specifying a file path. Set to `null` to disable.

```
breakStartSound: breakStart
breakFlashSound: breakFlash
breakEndSound: breakEnd
```

### Warning toggle

The `toggleHotkey` toggles the break warning dialog. This can be useful when watching videos or accessing something the dialog covers. When toggled back on, the break time is remembered.

```
toggleHotkey: ctrl shift alt Z
```

## Eye tracking

Clippy enables control of the mouse using your eyes and a [Tobii Eye Tracker 4C](https://tobiigaming.com/eye-tracker-4c/). Look where you want to click, then press the hotkey to click at that position.

```
tobiiEnabled: true
tobiiClickHotkey: CAPS_LOCK
```

When eye tracking is accurate, mouse control is unreal -- it feels like your brain is controlling the computer! However, often it is not perfectly accurate and can be off the target by 1-2" (2.5-5cm). Clippy solves this by using head tracking when you hold down the hotkey. Head tracking is much more accurate, but not suitable for moving the mouse across the entire screen. Clippy uses eye tracking to move the mouse large distances and head tracking to make fine adjustments.

When the hotkey is pressed, the mouse is placed at (or near) the position on the screen where you are looking. While holding the hotkey, move your head left, right, up, or down slightly to move the mouse. This allows you to adjust for any eye tracking inaccuracy. The mouse is clicked when you release the hotkey. Press shift while holding the hotkey to cancel head tracking without clicking.

If the mouse is moved while the hotkey is pressed, head tracking is cancelled without clicking. This enables eye tracking to teleport the mouse cursor near where you want it, and then you can use the mouse to adjust the position rather than head tracking. This is similar to Tobii's "mouse warp", but is triggered only when you press the hotkey rather than all the time.

The sensitivity settings control how head movement (millimeters) is translated to mouse cursor movement (pixels). Higher numbers cause head movement to move the mouse cursor more. It can be helpful to use a higher sensitivity for the Y axis, since it's more difficult to move your head up and down.

```
tobiiHeadSensitivityX: 5
tobiiHeadSensitivityY: 7
```

## Lighting control

Clippy can connect to a [Philips Hue](http://meethue.com) bridge and adjust the color of lights based on the time of day. `philipsHueEnabled` must be set to true to enable connecting to the Philips Hue bridge. Most users won't configure `philipsHueIP` and `philipsHueUser` manually. Instead, leave them set to `null` and Clippy will search for your Philips Hue bridge and configure the two settings automatically.

```
philipsHueEnabled: true
philipsHueIP: null
philipsHueUser: null
```

When Clippy is first run with these settings, it will prompt to press the link button of the Philips hue bridge. Afterward Clippy will store the bridge information and will connect automatically in the future.

```
philipsHueEnabled: true
philipsHueIP: 192.168.1.12
philipsHueUser: mqEFxXynUbikLs1VaKUrp1CdjCkK1lUYCGzopxA
```

### Timelines

The `philipsHue` setting has a list of lights, each with 1 or more timelines which work in the same way as the [gamma](#blue-light-reduction) setting.

```
philipsHue: [
	{
		name: Desk lamp
		model: null
		switch: null
		timelines: {
			on: [
				{ time: sunrise:43.4357, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 7:00pm, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
				{ time: 4:30am, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
			]
		}
	}
	{
		name: group:Bedroom
		model: LCT001
		switch: null
		timelines: {
			on: [
				{ time: 1:00pm, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 2:00pm, brightness: 1, r: 0.6, g: 0, b: 0 }
				{ time: sunset:43.4357, brightness: 1, r: 1, g: 1, b: 1 }
			]
		}
	}
]
```

`name` is the name of a single light which will be controlled by the section. If `name` is `null`, the section will control all lights. If `name` begins with `group:` then the section will control that group, for example `group:Office` would control the group named `Office`. Groups can be setup using the Hue mobile phone app, which calls them "rooms".

The `on` timeline is used when the light is turned on normally. See [switches](#switches) below for switching to other timelines.

Colors are specified as described [above](#colors). Each Philips Hue light model supports a specific [color gamut](http://www.developers.meethue.com/documentation/supported-lights). When setting the color of all lights or for a group, the `model` setting is used to convert the r, g, b into a color the specified light model can use. When setting the color of a single light, the light's actual model is used and the `model` setting is ignored.

### Power

Lights can be turned on or off by specifying the power state:

```
philipsHue: [
	{
		name: Desk lamp
		model: null
		switch: null
		timelines: {
			on: [
				{ time: sunrise:43.4357, power: on, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 7:00pm, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
				{ time: 4:30am, power: off, brightness: 0.8, r: 1, g: 0.8, b: 0.6 }
			]
		}
	}
]
```

### Switches

Clippy has special support for Philips Hue Dimmer Switches, which make it very convenient to turn lights on and off, dim or brighten lights, and to switch between various lighting timelines while still allowing Clippy to control the lights color and brightness based on the time of day.

If light colors are changed using the Hue mobile app or other software, Clippy will overwrite the changes the next time it sends a command based on the time of day. Clippy would need to query every light to detect that it has changed, which is not feasible when controlling groups of lights or all lights. Because of this, you will want to use one or more Philips Hue Dimmer Switches to control your lights.

#### Setup

The `switch` setting can be set to the name of the dimmer switch used to control lights in that section. When the switch is used to dim or brighten, Clippy will cease controlling brightness (but still controls color) for the number of minutes specified by `philipsHueDisableMinutes`. To return control to Clippy sooner, press the on button momentarily or hold the on or off buttons to change to an [alternate timeline](#alternate-timelines).

In the Hue mobile phone app, edit the switch and set rooms for the on button to define which lights it will control. Next, set each of the 5 presses to "last state". This will avoid the lights from flashing, since when the on button is pressed you'd see the scene lighting briefly before Clippy applies its lighting.

When the Hue mobile phone app adds a new switch, it sets up rules for the 4 switch buttons. The rule for the off button is to turn off the lights as soon as the button goes down, which would mean that the light is off by the time Clippy changes to the `offHeld` timeline. To fix this, Clippy automatically modifies the off button rule when an `offHeld` timeline is defined to happen only when the off button is pressed momentarily. To restore the old behavior, run Clippy without an `offHeld` timeline defined.

#### Alternate timelines

Specifying a switch name enables alternate timelines by holding the on or off buttons:

```
philipsHue: [
	{
		name: group:Bedroom
		model: LCT001
		switch: Bedroom switch
		timelines: {
			on: [
				{ time: 1:00pm, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 2:00pm, brightness: 1, r: 0.6, g: 0, b: 0 }
				{ time: sunset:43.4357, brightness: 1, r: 1, g: 1, b: 1 }
			]
			onHeld: [
				{ time: 12:00am, brightness: 1, r: 1, g: 1, b: 1 }
			]
			offHeld: [
				{ time: 12:00am, brightness: 0.15, r: 1, g: 0.7, b: 0.5 }
				{ time: 4:00pm, brightness: 1, r: 1, g: 0, b: 0 }
			]
		}
	}
]
```

The `on` timeline is used when the light is turned on normally. The `onHeld` and `offHeld` timelines are used after the respective on or off buttons are held down for a few seconds. To return to the `on` timeline, press the on button momentarily.

If an `onHeld` or `offHeld` timeline has only one entry and that entry does not have a time, then after the one entry is applied the `on` timeline is set as the active timeline. Using this sort of "momentary" timeline allows holding a button to turn everything off but without staying on that timeline, which gives another timeline the ability to turn the lights back on. For example, this configuration allows holding off to turn off everything, but the `on` timeline will still turn the lights on in the morning.

```
{
	name: group:Bedroom
	model: LCT001
	switch: Bed switch
	timelines: {
		offHeld: [
			{ power: off, brightness: 0, r: 0, g: 0, b: 0 }
		]
		on: [
			{ time: 7:00am, power: on, brightness: 0, r: 1, g: 0, b: 0 }
			{ time: 7:30am, brightness: 1, r: 1, g: 1, b: 1 }
			{ time: 10:59pm, brightness: 0.74, r: 1, g: 0.7, b: 0.5 }
			{ time: 11:00pm, brightness: 0.6, r: 1, g: 0, b: 0 }
		]
	}
}
```

#### Multiple switches

Multiple switches can be used to control the same lights by defining multiple sections with the same name but different switches:

```
philipsHue: [
	{
		name: group:Office
		model: LCT001
		switch: Door switch
		timelines: {
			on: [
				{ time: 1:00pm, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 2:00pm, brightness: 1, r: 0.6, g: 0, b: 0 }
				{ time: sunset:43.4357, brightness: 1, r: 1, g: 1, b: 1 }
			]
			onHeld: [
				{ time: 12:00am, brightness: 1, r: 1, g: 1, b: 1 }
			]
			offHeld: [
				{ time: 12:00am, brightness: 0.15, r: 1, g: 0.7, b: 0.5 }
			]
		}
	}
	{
		name: group:Office
		model: LCT001
		switch: Desk switch
		timelines: {
			on: [
				{ time: 1:00pm, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 2:00pm, brightness: 1, r: 0.6, g: 0, b: 0 }
				{ time: sunset:43.4357, brightness: 1, r: 1, g: 1, b: 1 }
			]
			onHeld: [
				{ time: 12:00am, brightness: 1, r: 1, g: 0, b: 0 }
			]
			offHeld: [
				{ time: 12:00am, brightness: 1, r: 0, g: 1, b: 0 }
			]
		}
	}
]
```

In this example, the `on` timelines are identical but `onHeld` and `offHeld` differ.

Similarly, a switch may appear in multiple sections.

#### Example settings

The settings below shows a real life configuration meant to both be convenient and to reduce blue light in the hours before bedtime. The room is a bedroom and has a computer desk. It has 2 lamps in the "Lights" group and a Philips Hue LightStrip+ named "Headboard" which is attached to the headboard behind the bed. There are two switches, one by the bed and one by the computer desk.

The lamps start with a white color at 5AM. They don't turn on automatically at that time but if they are on, they will be a comfortable white. It stays relatively white during the day, getting a bit dimmer around 7PM which is near sunset in most places. From 9PM to midnight it gets dimmer and more red to reduce blue light exposure before bed, but is still quite reasonable to read or use the computer. At midnight the lamps turn bright red for 1 minute to indicate bedtime. By 1AM the lamps become quite dim and red. From 1AM to 4:30AM they transition to extremely dim and very red, but hopefully you have gone to bed long before this!

The headboard light turns on automatically at 7AM using a dim red color. From 8AM to 8:30AM it transitions from red to yellow to white, emulating a sunrise. This way if you wake up and aren't sure of the time, if the headboard is off you know to just go back to sleep. If the headboard is red, you know it's early but not a terribly unreasonable time to wake up. If it's white, you know you should get out of bed.

From 8PM to 11PM the headboard gets more red to match the lamps. From 11PM to midnight it gets even more red. At midnight it gets very red for 1 minute, then turns off to indicate bedtime.

For the desk switch, on/off and dim/bright control the lamps. Holding on turns the lamps and headboard on full blast for cleaning, finding something, working on electronics, etc. Holding off turns the lamps and headboard to a dim red for eating while watching TV or a movie.

For the bed switch, on/off and dim/bright control the lamps. Holding on turns off the lamps and sets the headboard light for reading at night. Holding off turns off the lamps and headboard.

The computer monitor starts with full brightness at 5AM. From 5PM to 9PM it transitions to more red and a bit dimmer, to match the sunset. By about 11:30PM it is noticeably red and probably a good time to stop using it. From 12:30AM to 5AM it becomes even more dim and red.

```
philipsHue: [
	{
		name: group:Lights
		model: LCT007
		switch: Bed switch
		timelines: {
			offHeld: [
				{ time: 12:00am, power: off, brightness: 0, r: 0, g: 0, b: 0 }
			]
			onHeld: [
				{ time: 12:00am, power: off, brightness: 0, r: 0, g: 0, b: 0 }
			]
			on: [
				{ time: 12:00am, brightness: 0.4, r: 1, g: 0.6, b: 0.42 }
				{ time: 12:01am, brightness: 0, r: 1, g: 0, b: 0 }
				{ time: 12:02am, brightness: 0.37, r: 1, g: 0.55, b: 0.37 }
				{ time: 1:00am, brightness: 0.37, r: 1, g: 0.55, b: 0.37 }
				{ time: 4:30am, brightness: 0.25, r: 1, g: 0.4, b: 0.25 }
				{ time: 5:00am, brightness: 1, r: 1, g: 0.94, b: 0.85 }
				{ time: 5:00pm, brightness: 1, r: 1, g: 0.84, b: 0.6 }
				{ time: 7:00pm, brightness: 0.74, r: 1, g: 0.75, b: 0.59 }
				{ time: 9:00pm, brightness: 0.74, r: 1, g: 0.72, b: 0.54 }
			]
		}
	}
	{
		name: group:Lights
		model: LCT007
		switch: Desk switch
		timelines: {
			offHeld: [
				{ time: 12:00am, power: on, brightness: 0, r: 1, g: 0, b: 0 }
			]
			onHeld: [
				{ time: 12:00am, power: on, brightness: 1, r: 1, g: 1, b: 1 }
			]
			on: [
				{ time: 12:00am, brightness: 0.4, r: 1, g: 0.6, b: 0.42 }
				{ time: 12:01am, brightness: 0, r: 1, g: 0, b: 0 }
				{ time: 12:02am, brightness: 0.37, r: 1, g: 0.55, b: 0.37 }
				{ time: 1:00am, brightness: 0.37, r: 1, g: 0.55, b: 0.37 }
				{ time: 4:30am, brightness: 0.25, r: 1, g: 0.4, b: 0.25 }
				{ time: 5:00am, brightness: 1, r: 1, g: 0.94, b: 0.85 }
				{ time: 5:00pm, brightness: 1, r: 1, g: 0.84, b: 0.6 }
				{ time: 7:00pm, brightness: 0.74, r: 1, g: 0.75, b: 0.59 }
				{ time: 9:00pm, brightness: 0.74, r: 1, g: 0.72, b: 0.54 }
			]
		}
	}
	{
		name: Headboard
		model: null
		switch: Bed switch
		timelines: {
			offHeld: [
				{ power: off, brightness: 0, r: 0, g: 0, b: 0 }
			]
			onHeld: [
				{ time: 12:00am, power: on, brightness: 0.35, r: 1, g: 0.5, b: 0.35 }
			]
			on: [
				{ time: 12:00am, brightness: 0.6, r: 1, g: 0, b: 0 }
				{ time: 12:01am, power: off, brightness: 0, r: 0, g: 0, b: 0 }
				{ time: 7:00am, power: on, brightness: 0, r: 1, g: 0, b: 0 }
				{ time: 8:00am, brightness: 0.35, r: 1, g: 0, b: 0 }
				{ time: 8:05am, brightness: 0.5, r: 1, g: 0, b: 0 }
				{ time: 8:10am, brightness: 0.5, r: 1, g: 0.5, b: 0 }
				{ time: 8:15am, brightness: 1, r: 1, g: 1, b: 0 }
				{ time: 8:20am, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 8:25am, brightness: 0.74, r: 1, g: 0.7, b: 0.5 }
				{ time: 8:26am, brightness: 1, r: 0, g: 0, b: 1 }
				{ time: 8:29am, brightness: 1, r: 0, g: 0, b: 1 }
				{ time: 8:30am, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 1:52pm, power: on, brightness: 1, r: 1, g: 0, b: 0 }
				{ time: 6:00pm, brightness: 0.85, r: 1, g: 0.85, b: 0.65 }
				{ time: 8:00pm, brightness: 0.74, r: 1, g: 0.7, b: 0.5 }
				{ time: 11:00pm, brightness: 0.5, r: 1, g: 0.6, b: 0.4 }
				{ time: 11:59pm, brightness: 0.35, r: 1, g: 0.5, b: 0.35 }
			]
		}
	}
	{
		name: Headboard
		model: null
		switch: Desk switch
		timelines: {
			offHeld: [
				{ time: 12:00am, brightness: 0.2, r: 1, g: 0, b: 0 }
			]
			onHeld: [
				{ time: 12:00am, power: on, brightness: 1, r: 1, g: 1, b: 1 }
			]
			on: [
				{ time: 12:00am, brightness: 0.6, r: 1, g: 0, b: 0 }
				{ time: 12:01am, power: off, brightness: 0, r: 0, g: 0, b: 0 }
				{ time: 7:00am, power: on, brightness: 0, r: 1, g: 0, b: 0 }
				{ time: 8:00am, brightness: 0.35, r: 1, g: 0, b: 0 }
				{ time: 8:05am, brightness: 0.5, r: 1, g: 0, b: 0 }
				{ time: 8:10am, brightness: 0.5, r: 1, g: 0.5, b: 0 }
				{ time: 8:15am, brightness: 1, r: 1, g: 1, b: 0 }
				{ time: 8:20am, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 8:25am, brightness: 0.74, r: 1, g: 0.7, b: 0.5 }
				{ time: 8:26am, brightness: 1, r: 0, g: 0, b: 1 }
				{ time: 8:29am, brightness: 1, r: 0, g: 0, b: 1 }
				{ time: 8:30am, brightness: 1, r: 1, g: 1, b: 1 }
				{ time: 1:52pm, power: on, brightness: 1, r: 1, g: 0, b: 0 }
				{ time: 6:00pm, brightness: 0.85, r: 1, g: 0.85, b: 0.65 }
				{ time: 8:00pm, brightness: 0.74, r: 1, g: 0.7, b: 0.5 }
				{ time: 11:00pm, brightness: 0.5, r: 1, g: 0.6, b: 0.4 }
				{ time: 11:59pm, brightness: 0.35, r: 1, g: 0.5, b: 0.35 }
			]
		}
	}
]
gamma: [
	{ time: 12:30am, brightness: 0.67, r: 1, g: 0.56, b: 0.3 }
	{ time: 4:59am, brightness: 0.65, r: 1, g: 0.48, b: 0.25 }
	{ time: 5:00am, brightness: 1, r: 1, g: 1, b: 1 }
	{ time: 5:00pm, brightness: 1, r: 1, g: 0.99, b: 0.96 }
	{ time: 7:00pm, brightness: 0.9, r: 1, g: 0.97, b: 0.87 }
	{ time: 9:00pm, brightness: 0.8, r: 1, g: 0.75, b: 0.5 }
	{ time: 11:30pm, brightness: 0.7, r: 1, g: 0.64, b: 0.4 }
]
```

## Dyanmic DNS

When your IP changes, Clippy can update your dynamic DNS entry at [DnsMadeEasy](http://dnsmadeeasy.com) so you can always access your computer.

```
dnsUser: Meow
dnsPassword: password123
dnsID: 1738294
dnsMinutes: 30
```

The user and password settings are your DnsMadeEasy account credentials. You may optionally configure DnsMadeEasy to have a password per record, so you don't need to use your account password. Record ID identifies the record to update. Minutes is the number of minutes between IP checks.

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

While Clippy is written in Java and most of it is platform agnostic, a number of Windows specific APIs are needed: accessing the clipboard events and contents, global hotkeys, showing the history popup at the cursor location, and more. These could be abstracted so Clippy can be ported to other languages, but since Clippy's author uses Windows there is not much motivation for such effort.

## License

Clippy is released as OSS under the [New BSD license](https://github.com/EsotericSoftware/clippy/blob/master/LICENSE).
