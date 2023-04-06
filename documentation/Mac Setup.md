# MacOS
- set country and region
- set accesiblity settings if applicable
- connect to a wii network
- when promted to transfer your data choose not now
- do not sign in with your apple id
- do not select express setup
- do not setup icloud
- do not setup screen time
- do not setup siri
- optional: enable touch id
- do not setup apple pay
- use defualt look, do not customize

# Applications
- Open safari
- download rewind https://www.rewind.ai/ (contact your IT admin for billing information if promted)
- open the downloaded file and follow the instalation process
- enter password if promted
- configure rewind
	- exclude voice memos from being recorded
	  settings > screen > excluded apps > check the box next to voice memos
	- enable private browsing
	  settings > screen > check the box next to private browsing
- in safari download arc browser (you will need to use the invite code that was shared with you)
- install and configure arc browser
- using arc browser download, install and cofigure raycast https://www.raycast.com/
- other software will need to be downloaded, installed, and configured
	-   Zed (use invite code provided to you) 
	-   Warp https://www.warp.dev/blog/introducing-warp
	-   Zulip https://zulip.com/
	-   Github https://desktop.github.com/
	-   Obsidian https://obsidian.md/ (will be configured later in the obsidian section)
	-   Skiff (use invite code provided to you to create new skiff account) 
		- make email alias following this format: firstname@plurigrid.xyz

# Websites
- gather town
	- follow this link to create a gather town account https://app.gather.town/invite?token=jUS1161xQBSMNr6YmixT
- chatorg use the link provided to you to join the plurigrid chat org (this is where you will conduct all agentic loops)

# Configuration
- ensure that macos is up to date
	- system settings > general > software update
- desktop and dock
	- set dock size to slider 1/3 to the left (twards the small end)
	- set magnification to off
	- enable show indicators for application icons
	- enable close windows when quitting an application 
	- enable stage manger
	- enable automaticlly rearange spaces based on most recent use
	- enable group windows by application
- game center
	- enable nearby players
	- enable connect with friends

# Obsidian
In a terminal emulator run the following commands

```bash
curl -L https://nixos.org/nix/install | sh
nix-env -iA just && just play
```

To debug the open game:
```
nix-shell
poetry shell
python scripts/http.py
```

If you want to exit the shell environment and return to your original shell, you can simply type `exit` or press `Ctrl-D`.

To prepare your pluralistic interface in Obsidian, simply use the appropriate template under `.playback`.

Then run `just obsidian`.

See the obsidian installation instructions below for more details.

## Setting Up Obsidian and Plugins

1. **Set up Obsidian**
- Use gitgub to clone the ontology repo and open it as a vault. https://github.com/plurigrid/ontology

2. **Set up the Smart Connections Plugin**
- Go to Settings (gear icon) > "Community plugins" > "Turn off Restricted Mode" Then, "Browse community plugins".
- Search for "Smart Connections" and install the plugin.
- Activate the Smart Connections plugin in the "Installed plugins" tab.
- Configure the plugin to your preferences and start creating AI-powered note connections by following the plugin's documentation: https://github.com/mgmeyers/obsidian-smartconnections
- Make sure you set your Open AI API key for GPT-4 in the plugin settings.

3. **Set Up Obsidian Git Plugin**
- Install the Obsidian Git plugin following the steps for the previous section, but search for the "Obsidian Git" plugin.
- Plugin documentation can be found here: https://publish.obsidian.md/git-doc/01+Start+here
- Follow the installation and authentication steps from the documentation.
- **Configure Obsidian Git settings**: Go to `Settings` > `Plugins Options` > `Obsidian Git` to configure your settings. For example settings, see `.config/data.json` in this repository and copy it over to `.obsidian/plugins/obsidian-git/data.json`.

### Workflow with Obsidian Git and Smart Connections

- Before you begin, run the "Obsidian Git: Pull" command to make sure your workspace is up to date.
- When you start your journey, create a new branch that is topical to the journey. Use the "Obsidian Git: Create new branch" command.
- With the default settings, your work should automatically be pushed and merged every 60 minutes. Your repository will also be automatically pulled on the same cadence.
- As you contribute to ontology, you can use the "Smart Connections: Smart Chat conversation" command to ask an agent questions over your data. Use the ontology agent as a guide to help your personal loops.