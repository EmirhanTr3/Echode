package cat.emir.echode.listener

import org.bukkit.event.Event
import org.bukkit.event.Listener

class AnyEventListener<T : Event>(val event: Class<T>) : Listener