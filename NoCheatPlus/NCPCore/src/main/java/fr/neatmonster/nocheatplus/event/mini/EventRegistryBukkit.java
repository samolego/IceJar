/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.event.mini;

import java.lang.reflect.Method;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.components.registry.feature.ComponentWithName;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * A MultiListenerRegistry that registers Bukkit types with a Spigot/CraftBukkit
 * server.
 * 
 * <br>
 * Listener registration for {@link org.bukkit.event.Listener}, checking methods
 * for {@link org.bukkit.event.EventHandler}. <br>
 * <br>
 * Supports passing a defaultOrder, as well as the per-class annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder},
 * and the per-method annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder}.
 * <br>
 * Priority (FCFS): RegisterMethodWithOrder, RegisterEventsWithOrder,
 * defaultOrder
 * 
 * <br>
 * <br>
 * For alternatives and more details and conventions see:
 * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry}<br>
 * 
 * 
 * @author asofold
 *
 */
public class EventRegistryBukkit extends MultiListenerRegistry<Event, EventPriority> {

    /**
     * Node for events that implement the Cancellable interface (Bukkit).
     * 
     * @author asofold
     * 
     * @param <E>
     */
    protected static class CancellableNodeBukkit<E> extends MiniListenerNode<E, EventPriority> {

        public CancellableNodeBukkit(Class<E> baseType, EventPriority basePriority) {
            super(baseType, basePriority);
        }

        // TODO: Future java: E extends Cancellable ?
        @Override
        protected boolean isCancelled(E event) {
            return ((Cancellable) event).isCancelled();
        }
    }

    private final Plugin plugin;

    /**
     * Pass this listener with each event registration to the
     * {@link org.bukkit.plugin.PluginManager}.
     */
    private final Listener dummyListener = new Listener() {}; // TODO: Get from NCP ?

    public EventRegistryBukkit(Plugin plugin) {
        this.plugin = plugin;
        nodeFactory = new NodeFactory<Event, EventPriority>() {
            @Override
            public <E extends Event> MiniListenerNode<E, EventPriority> newNode(Class<E> eventClass, EventPriority basePriority) {
                if (Cancellable.class.isAssignableFrom(eventClass)) {
                    // TODO: Check if order is right (eventClass extends Cancellable).
                    // TODO: Future java (see above) ?
                    return new CancellableNodeBukkit<E>(eventClass, basePriority);
                } else {
                    return new MiniListenerNode<E, EventPriority>(eventClass, basePriority);
                }
            }
        };
        // Auto register for plugin disable.
        // TODO: Ensure the ignoreCancelled setting is correct (do listeners really not unregister if the event is cancelled).
        register(new MiniListener<PluginDisableEvent>() {
            @Override
            public void onEvent(PluginDisableEvent event) {
                unregisterAttached(event.getPlugin());
            }
        }, EventPriority.MONITOR, new RegistrationOrder("nocheatplus.system.registry", null, ".*"), true);
    }

    @Override
    protected <E extends Event> void registerNode(final Class<E> eventClass, 
            final MiniListenerNode<E, EventPriority> node, final EventPriority basePriority) {
        Bukkit.getPluginManager().registerEvent(eventClass, 
                dummyListener,
                basePriority, new EventExecutor() {
            @SuppressWarnings("unchecked")
            @Override
            public void execute(Listener dummy, Event event) throws EventException {
                if (eventClass.isAssignableFrom(event.getClass())) {
                    node.onEvent((E) event);
                }
            }
        }, plugin, false);
    }

    /**
     * Convenience method to have a listener unregister with disabling a plugin.
     * 
     * @param listener
     *            Do not call with a plugin class being the listener, use the
     *            other register method instead!
     * @param plugin
     */
    public void register(Listener listener, Plugin plugin) {
        register(listener, null, plugin);
    }

    /**
     * Convenience method to have a listener unregister with disabling a certain
     * other plugin.
     * 
     * @param listener
     *            Do not call with a plugin class being the listener, use the
     *            other register method instead!
     * @param defaultOrder
     * @param plugin
     * @see {@link #register(Listener, RegistrationOrder)}
     */
    public void register(Listener listener, RegistrationOrder defaultOrder, Plugin plugin) {
        attach(internalRegister(listener, defaultOrder), plugin);
    }

    /**
     * Register the given listener similar to
     * {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}.
     * <br>
     * All events are registered for the plugin that was passed upon creation of
     * this registry (supposedly NoCheatPlus).
     * 
     * @param listener
     * @see {@link #register(Listener, RegistrationOrder)}
     */
    public void register(Listener listener) {
        register(listener, (RegistrationOrder) null);
    }

    /**
     * Register the given listener similar to
     * {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}.
     * <br>
     * All events are registered for the plugin that was passed upon creation of
     * this registry (supposedly NoCheatPlus). Methods are selected by presence
     * of the annotation {@link org.bukkit.event.EventHandler}.
     * <hr>
     * Supports passing a defaultOrder, as well as the per-class annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder},
     * and the per-method annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder}.
     * <br>
     * Priority (FCFS): RegisterMethodWithOrder, RegisterEventsWithOrder,
     * defaultOrder
     * 
     * <br>
     * <br>
     * If no order is given, but the listener implements ComponentWithName, a
     * RegistrationOrder with the component name as tag is created. <br>
     * <br>
     * <br>
     * For alternatives and more details and conventions see:
     * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry}<br>
     * 
     * @param listener
     * @param defaultOrder
     */
    public void register(Listener listener, RegistrationOrder defaultOrder) {
        internalRegister(listener, defaultOrder);
    }

    /**
     * Convenience method for component registration. Use @EventHandler to
     * specify EventPriority and ignoreCancelled. See
     * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry#register(Class, MiniListener, Object, RegistrationOrder, boolean)}
     * for reference on how to set up order.
     * 
     * @param eventClass Type of event to register for.
     * @param listener
     * @throws IllegalArgumentException
     *             If @EventHandler is not present or mismatching types.
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void register(Class<E> eventClass, MiniListener<E> listener) {
        final Class<MiniListener<E>> clazz = (Class<MiniListener<E>>) listener.getClass();
        EventHandler eh;
        try {
            Method method = clazz.getMethod("onEvent", eventClass);
            if (!method.isAnnotationPresent(EventHandler.class)) {
                throw new IllegalArgumentException("Must have @EventHandler annotation: " + clazz.getName());
            }
            eh = method.getAnnotation(EventHandler.class);
            eventClass = (Class<E>) method.getParameterTypes()[0];
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
        register(eventClass, listener, eh.priority(), null, eh.ignoreCancelled());
    }

    /**
     * Convenience method for component registration. Use @EventHandler to
     * specify EventPriority and ignoreCancelled. See
     * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry#register(Class, MiniListener, Object, RegistrationOrder, boolean)}
     * for reference on how to set up order.
     * 
     * @param listener
     * @throws IllegalArgumentException
     *             If @EventHandler is not present or mismatching types.
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void register (MiniListener<E> listener) {
        // TODO: Throw something if there is more than one method, or implement differently.
        Class<E> eventClass;
        final Class<MiniListener<E>> clazz = (Class<MiniListener<E>>) listener.getClass();
        try {
            Method method = ReflectionUtil.seekMethodIgnoreArgs(clazz, "onEvent");
            eventClass = (Class<E>) method.getParameterTypes()[0];
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
        register(eventClass, listener);
    }

    private Collection<MiniListener<? extends Event>> internalRegister(Listener listener, 
            RegistrationOrder defaultOrder) {
        // Note: default ignoreCancelled and priority should have no effect, as EventHandler sets the defaults anyway.
        // NCP for convenience: tag by component name, if no order is given.
        if (defaultOrder == null && listener instanceof ComponentWithName) {
            defaultOrder = new RegistrationOrder(((ComponentWithName) listener).getComponentName());
        }
        return super.register((Object) listener, EventPriority.NORMAL, defaultOrder, false);
    }

    @Override
    protected boolean shouldBeEventHandler(Method method) {
        return method.getAnnotation(EventHandler.class) != null;
    }

    @Override
    protected boolean getIgnoreCancelled(Method method, boolean defaultIgnoreCancelled) {
        EventHandler info = method.getAnnotation(EventHandler.class);
        if (info == null) {
            return defaultIgnoreCancelled;
        }
        else {
            return info.ignoreCancelled();
        }
    }

    @Override
    protected EventPriority getPriority(Method method, EventPriority defaultPriority) {
        EventHandler info = method.getAnnotation(EventHandler.class);
        if (info == null) {
            return defaultPriority;
        }
        else {
            return info.priority();
        }
    }

}
