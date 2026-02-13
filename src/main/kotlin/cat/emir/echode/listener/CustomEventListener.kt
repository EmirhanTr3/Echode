package cat.emir.echode.listener

import org.bukkit.event.Event
import org.bukkit.event.Listener

class CustomEventListener<T : Event>(val event: Class<T>) : Listener