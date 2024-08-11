import json
from kivy.app import App
from kivy.uix.relativelayout import RelativeLayout
from kivy.uix.label import Label
from kivy.uix.button import Button
from kivy.core.audio import SoundLoader
from kivy.config import Config
from kivy.uix.popup import Popup
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.lang import Builder
from kivy.clock import Clock
from kivy.uix.image import Image
from kivy.uix.scrollview import ScrollView

# Завантаження KV файлів
try:
    Builder.load_file('info.kv')
    Builder.load_file('settings.kv')
    Builder.load_file('game.kv')
    print("KV файли завантажені успішно")
except Exception as e:
    print(f"Помилка завантаження KV файлів: {e}")

# Клас для управління звуками
class SoundManager:
    def __init__(self):
        self.active_sounds = []

    def play_sound(self, filepath):
        sound = SoundLoader.load(filepath)
        if sound:
            sound.play()
            self.active_sounds.append(sound)
        return sound

    def stop_all_sounds(self):
        for sound in self.active_sounds:
            if sound.state == 'play':
                sound.stop()
        self.active_sounds = []

# Створення глобального екземпляра SoundManager
sound_manager = SoundManager()

class MainScreen(RelativeLayout):
    def __init__(self, **kwargs):
        super(MainScreen, self).__init__(**kwargs)
        self.volume = 0.5
        self.sound = sound_manager.play_sound('raw/intro_full.wav')
        if self.sound:
            self.sound.loop = True
            self.sound.volume = self.volume

    def set_manager(self, manager):
        self.manager = manager

    def open_game(self):
        print("Game screen opened")
        self.manager.current = 'game'

    def open_settings(self):
        print("Settings screen opened")
        self.manager.current = 'settings'

    def open_info(self):
        print("Info screen opened")
        self.manager.current = 'info'

    def on_touch_down(self, touch):
        if self.ids.start_btn.collide_point(*touch.pos):
            self.open_game()
        elif self.ids.settings_btn.collide_point(*touch.pos):
            self.open_settings()
        elif self.ids.info_btn.collide_point(*touch.pos):
            self.open_info()
        return super(MainScreen, self).on_touch_down(touch)

    def on_pause(self):
        sound_manager.stop_all_sounds()

    def on_resume(self):
        self.sound = sound_manager.play_sound('raw/intro_full.wav')
        if self.sound:
            self.sound.loop = True
            self.sound.volume = self.volume

    def on_stop(self):
        sound_manager.stop_all_sounds()

class InfoScreen(Screen):
    pass

class SettingsScreen(Screen):
    pass

class GameScreen(Screen):
    def __init__(self, **kwargs):
        super(GameScreen, self).__init__(**kwargs)
        self.text_index = 0
        self.delay_between_characters = 0.04
        self.delay_between_texts = 2.0
        self.animation_in_progress = False
        self.history_block_is_visible = False
        self.text_array = []
        self.current_audio_path = None
        self.current_sound = None
        self.load_text_array()
        Clock.schedule_once(self.animate_text)
        sound_manager.stop_all_sounds()
        # Start text animation after initialization
        
    def go_to_menu(self):
        sound_manager.stop_all_sounds()
        self.manager.current = 'main'

    def load_text_array(self):
        try:
            with open('dialogues.json', 'r', encoding='utf-8') as f:
                self.text_array = json.load(f)
                print(f"Loaded text_array: {self.text_array}")
        except FileNotFoundError:
            print("dialogues.json file not found")
        except json.JSONDecodeError:
            print("Error decoding dialogues.json")

    def show_history(self):
        if not self.history_block_is_visible:
            self.history_block_is_visible = True
            self.history_popup = Popup(title='History', size_hint=(None, None), size=(300, 200))
            content = BoxLayout(orientation='vertical')
            for i in range(self.text_index):
                pair = self.text_array[str(i)]
                label = Label(text=f"{pair['name']}: {pair['text']}", font_size='14sp', halign='left', valign='middle')
                content.add_widget(label)
            self.history_popup.content = content
            self.history_popup.open()
        else:
            self.history_popup.dismiss()
            self.history_block_is_visible = False

    def quick_load(self):
        self.ids['text'].text = ""
        self.text_index = 0

    def quick_save(self):
        with open('save.json', 'w', encoding='utf-8') as f:
            json.dump({'text_index': self.text_index}, f)

    def animate_text(self, dt=0):
        if self.text_index < len(self.text_array):
            pair = self.text_array.get(str(self.text_index), {})
            
            # Перевірка наявності id 'name'
            if 'name' in self.ids:
                self.ids['name'].text = pair.get('name', "Кейт")
            
            # Перевірка наявності id 'text'
            if 'text' in self.ids:
                self.ids['text'].text = ""
            
            text_to_animate = pair.get('text', "")
            image_path = pair.get('image', "")
            audio_path = pair.get('audio', "")
            self.animation_in_progress = True
            self._animate_text(text_to_animate)
            
            if 'image' in self.ids:
                if image_path:
                    self.ids['image'].source = image_path
                else:
                    self.ids['image'].source = ""

            if audio_path:
                if audio_path == "NULL":
                    sound_manager.stop_all_sounds()
                    self.current_audio_path = None
                elif audio_path and audio_path != self.current_audio_path:
                    self.current_audio_path = audio_path
                    sound_manager.stop_all_sounds()
                    sound_manager.play_sound(audio_path)
        else:
            if 'text' in self.ids:
                self.ids['text'].text = ""

    def _animate_text(self, text_to_animate, i=0):
        if i < len(text_to_animate):
            self.ids['text'].text += text_to_animate[i]
            i += 1
            Clock.schedule_once(lambda dt: self._animate_text(text_to_animate, i), self.delay_between_characters)
        else:
            self.text_index += 1
            self.animation_in_progress = False
            Clock.schedule_once(self.animate_text, self.delay_between_texts)

    def first_btn(self):
        if self.text_index == 10:
            self.text_index = 11
        elif self.text_index == 155:
            self.text_index = 158
        self.animate_text()

    def second_btn(self):
        if self.text_index == 20:
            self.text_index = 21
        elif self.text_index == 255:
            self.text_index = 258
        self.animate_text()

class RomanticAdventureApp(App):
    def build(self):
        self.title = "Romantic Adventure"
        Config.set('graphics', 'fullscreen', 'auto')

        sm = ScreenManager()
        main_screen = MainScreen()
        main_screen.set_manager(sm)
        sm.add_widget(Screen(name='main'))
        sm.add_widget(Screen(name='info'))
        sm.add_widget(SettingsScreen(name='settings'))
        sm.add_widget(GameScreen(name='game'))

        sm.get_screen('main').add_widget(main_screen)
        sm.get_screen('info').add_widget(InfoScreen())

        return sm

    def on_pause(self):
        self.root.current_screen.on_pause()
        return True

    def on_resume(self):
        self.root.current_screen.on_resume()

    def on_stop(self):
        self.root.current_screen.on_stop()

    def on_back_button(self):
        content = BoxLayout(orientation='vertical')
        content.add_widget(Label(text='Are you sure you want to exit?'))
        button_layout = BoxLayout(orientation='horizontal')
        button_layout.add_widget(Button(text='Yes', on_release=self.stop))
        button_layout.add_widget(Button(text='No', on_release=lambda x: popup.dismiss()))
        content.add_widget(button_layout)
        popup = Popup(title='Exit Confirmation', content=content, size_hint=(0.8, 0.4))
        popup.open()
        return True

if __name__ == '__main__':
    RomanticAdventureApp().run()
