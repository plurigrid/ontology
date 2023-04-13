import json
import subprocess

def get_mac_theme():
    try:
        result = subprocess.check_output(
            ["defaults", "read", "-g", "AppleInterfaceStyle"], text=True
        )
        return result.strip().lower()
    except subprocess.CalledProcessError:
        return "light"

def update_zed_theme(theme):
    zed_settings_file = "/Users/aja/.config/zed/settings.json"

    with open(zed_settings_file, "r+") as f:
        settings = json.load(f)

        if theme == "dark":
            settings["theme"] = "Atelier Sulphurpool Dark"
        else:
            settings["theme"] = "Atelier Sulphurpool Light"

        f.seek(0)
        json.dump(settings, f, indent=4)
        f.truncate()

if __name__ == "__main__":
    mac_theme = get_mac_theme()
    update_zed_theme(mac_theme)
