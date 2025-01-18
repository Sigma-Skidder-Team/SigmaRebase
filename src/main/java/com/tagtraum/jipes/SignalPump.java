/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;
import java.util.*;

/**
 * <p>Pumps a signal from a {@link SignalSource} through a graph of {@link com.tagtraum.jipes.SignalProcessor}s,
 * also known as processing pipelines and lets you collect the results in a {@link Map}.
 * After being added to this pump, the pipelines are optimized to avoid
 * redundant processing, if at all possible.
 * </p>
 * <p>
 * To use a pump, <ol>
 * <li>Set a {@link SignalSource}.</li>
 * <li>Then {@link #add(com.tagtraum.jipes.SignalProcessor)} one or more already configured processors or pipelines.</li>
 * <li>Call {@link #pump()}.</li>
 * <li>Collect the results in the form of an id/output map.</li>
 * </ol> 
 * </p>
 *
 * @param <I> type of the input values from the associated {@link SignalSource}
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SignalPump<I> {

    private SignalSource<I> signalSource;
    private Map<SignalProcessor<I, ?>, SignalProcessor<I, ?>> rootProcessors = new LinkedHashMap<SignalProcessor<I, ?>,SignalProcessor<I, ?>>();
    private boolean cancelled;

    public SignalPump(final SignalSource<I> signalSource) {
        setSignalSource(signalSource);
    }

    public SignalPump() {
        this(null);
    }

    /**
     * Returns the currently set signal source.
     *
     * @return signal source
     */
    public SignalSource<I> getSignalSource() {
        return signalSource;
    }

    /**
     * Lets you set the {@link SignalSource} that this pump will use
     * as source.
     *
     * @param signalSource signal source
     */
    public void setSignalSource(final SignalSource<I> signalSource) {
        this.signalSource = signalSource;
    }

    /**
     * Lets you add a {@link com.tagtraum.jipes.SignalProcessor}-pipeline (that is a processor and its kids)
     * to this pump. To be of any use, most processors will have one or more children to execute multiple
     * processing steps. Internally, pipelines are optimized (i.e. re-organized) to drop redundant steps.
     * This implies, that you <em>must never</em> change the state of a processor or the makeup of a pipeline/graph,
     * after it has been added to the pipeline: results would be unpredictable.
     * <br>
     * Insertion order is preserved. This means {@link com.tagtraum.jipes.SignalProcessor#process(Object)},
     * and {@link com.tagtraum.jipes.SignalProcessor#flush()} are called on processors in the order they were added.
     *
     * @param signalProcessor ready to use processor with kids (a.k.a. pipeline)
     */
    public void add(final SignalProcessor<I, ?> signalProcessor) {
        final SignalProcessor<I, ?> processor = (SignalProcessor<I, ?>)decomposePipeline(signalProcessor);
        if (processor instanceof NoopSignalProcessor) {
            // this processor is actually multiple processors - let's add it as multiple roots
            for (final SignalProcessor<I, ?> p : ((NoopSignalProcessor<I>)processor).getConnectedProcessors()) {
                add(p);
            }
        } else {
            final SignalProcessor<?, ?> existingProcessor = rootProcessors.get(processor);
            if (existingProcessor != null) {
                final SignalProcessorPath[] signalProcessorPaths = SignalProcessorPath.create(processor);
                for (final SignalProcessorPath path : signalProcessorPaths) {
                    add(existingProcessor, path.path(), 1);
                }
            } else {
                rootProcessors.put(processor, processor);
            }
        }
    }

    private void add(final SignalProcessor<?, ?> processor, final SignalProcessor<?,?>[] path, final int index) {
        final SignalProcessor<?, ?> processorFromPath = path[index];
        final SignalProcessor<?, ?> child = getDuplicateChild(processor, processorFromPath);
        if (child != null) {
            if (path.length > index+1) add(child, path, index+1);
        } else {
            // this is cheating and could lead to type problems!! (hs)
            processor.connectTo((SignalProcessor) processorFromPath);
        }
    }

    /**
     * Returns the child of an existing processor, if it is equal to the provided potential child.
     *
     * @param existingProcessor processor already registered with this pump
     * @param potentialChild child that we might want to add.
     * @return the existing processor's child, which is equal to the potential child or null, if no such child can be found
     */
    private SignalProcessor<?, ?> getDuplicateChild(final SignalProcessor<?, ?> existingProcessor, final SignalProcessor<?, ?> potentialChild) {
        // never optimize demux processors, because they have multiple kinds of children (one or more for each channel)
        if (potentialChild instanceof SignalSplit) return null;
        for (final SignalProcessor<?, ?> child : existingProcessor.getConnectedProcessors()) {
            if (child.equals(potentialChild)) return child;
        }
        return null;
    }

    /**
     * 'Pumps' the provided {@link SignalSource} until it's dry and pushes the produced data into the configured
     * {@link com.tagtraum.jipes.SignalProcessor}s. Then collects the output by calling
     * {@link com.tagtraum.jipes.SignalProcessor#getOutput()} of all registered processors (and their children).
     * <p>
     * Note that every output/result id in all pipelines must be unique, if you actually want to be certain
     * that it is from the <em>one</em> processor you are interested in. Standard ids are <em>not</em> unique
     * (see {@link com.tagtraum.jipes.AbstractSignalProcessor#getId()}).
     *
     * @return a map of ids (see {@link com.tagtraum.jipes.SignalProcessor#getId()}}) and
     * their associated output (see {@link com.tagtraum.jipes.SignalProcessor#getOutput()})
     * @throws IOException if any IO problems occur
     * @throws IllegalStateException if the {@link SignalSource} is not set
     * @see com.tagtraum.jipes.AbstractSignalProcessor
     * @see com.tagtraum.jipes.audio.AudioSignalSource
     */
    public Map<Object, Object> pump() throws IOException, IllegalStateException {
        setCancelled(false);
        if (signalSource == null) throw new IllegalStateException("SignalSource must be set.");
        signalSource.reset();
        process();
        if (isCancelled()) {
            return null;
        }
        flush();
        return getOutput();
    }

    /**
     * Flushes all processors.
     *
     * @throws IOException if something goes wrong
     */
    private void flush() throws IOException {
        for (final SignalProcessor<?, ?> processor : rootProcessors.values()) {
            processor.flush();
        }
    }

    /**
     * Processes the buffers delivered by the signal source.
     *
     * @throws IOException if something goes wrong
     */
    private void process() throws IOException {
        if (rootProcessors.isEmpty()) {
            return;
        }
        I buffer;
        // create array for speed, to avoid having to create an iterator for each data chunk
        final SignalProcessor[] roots = rootProcessors.values().toArray(new SignalProcessor[rootProcessors.size()]);
        while ((buffer = signalSource.read()) != null) {
            for (final SignalProcessor<I, ?> rootProcessor : roots) {
                rootProcessor.process(buffer);
            }
            if (isCancelled()) return;
        }
    }

    /**
     * Cancels {@link #pump()}. May be called from another thread.
     */
    public void cancel() {
        setCancelled(true);
    }

    private synchronized void setCancelled(final boolean b) {
        this.cancelled = b;
    }

    /**
     * Indicates, whether a pump operation has been cancelled.
     *
     * @return true or false
     * @see #cancel()
     */
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    /**
     * Collects the output.
     *
     * @return output map
     * @throws IOException if something goes wrong
     */
    private Map<Object, Object> getOutput() throws IOException {
        final Map<Object, Object> results = new LinkedHashMap<Object, Object>();
        for (final SignalProcessor<?, ?> processor : rootProcessors.values()) {
            getOutput(processor, results);
        }
        return results;
    }

    private void getOutput(final SignalProcessor<?, ?> processor, final Map<Object, Object> results) throws IOException {
        final Object id = processor.getId();
        if (id != null) {
            final Object output = processor.getOutput();
            if (output != null) {
                results.put(id, output);
            }
        }
        for (SignalProcessor<?, ?>[] channelChildren : getChildren(processor)) {
            for (final SignalProcessor<?, ?> child : channelChildren) {
                getOutput(child, results);
            }
        }
    }

    /**
     * Returns the currently effective processing graphs.
     * Due to optimizations, this may not be the same as the pipelines added via
     * {@link #add(com.tagtraum.jipes.SignalProcessor)}.
     * <p/>
     * <em>This graph is read-only. Do not modify it.</em>
     *
     * @return set of processing graphs
     */
    public Set<SignalProcessor<I, ?>> getEffectiveProcessorGraphs() {
        return Collections.unmodifiableSet(rootProcessors.keySet());
    }

    /**
     * Produces a readable ASCII description of this pump and its pipelines.
     *
     * @return multi-line description
     */
    public String getDescription() {
        final StringBuilder sb = new StringBuilder();

        sb.append("SignalPump{generator=" + signalSource + "}\n\n");

        for (final SignalProcessor<I, ?> rootProcessor : rootProcessors.values()) {
            final ArrayList<Integer> indents = new ArrayList<Integer>();
            final String firstIndent = "   ";
            indents.add(firstIndent.length());
            sb.append(firstIndent + getDescription(rootProcessor, indents));
            sb.append('\n');
            sb.append('\n');
        }
        return sb.substring(0, sb.length() - 2);
    }

    private String getDescription(final SignalProcessor<?, ?> processor, final List<Integer> indents) {
        final StringBuilder sb = new StringBuilder();
        // hide pipelines
        if (!(processor instanceof SignalPipeline)) {
            sb.append("-[");
            sb.append(processor.toString());
            sb.append(']');
            indents.add(sb.length());
        } else {
            sb.append("-<Pipeline@");
            sb.append(processor.hashCode());
            sb.append('>');
            indents.add(sb.length());
        }

        final SignalProcessor<?, ?>[][] childProcessors = getChildren(processor);
        for (int channel = 0; channel<childProcessors.length; channel++) {
            for (int i=0; i<childProcessors[channel].length; i++) {
                final SignalProcessor<?,?> childProcessor = childProcessors[channel][i];
                if (i != 0 || channel != 0) {
                    sb.append('\n');
                    for (int k=0; k<indents.size(); k++) {
                        int indent = indents.get(k);
                        if (k>1) {
                            sb.append(" |");
                        }
                        for (int j=0; j<indent; j++) {
                            sb.append(' ');
                        }
                    }
                    sb.append(" +");
                } else {
                    sb.append("-+");
                }
                if (childProcessors.length > 1) {
                    sb.append("-channel" + channel);
                }
                sb.append(getDescription(childProcessor, new ArrayList<Integer>(indents)));
            }
        }
        return sb.toString();
    }

    /**
     * Returns the children for all channels.
     *
     * @param processor a processor
     * @return children in a channel array
     */
    private static SignalProcessor<?, ?>[][] getChildren(final SignalProcessor<?, ?> processor) {
        final SignalProcessor<?,?>[][] childProcessors;
        if (processor instanceof SignalSplit) {
            final SignalSplit<?,?> split = (SignalSplit<?,?>)processor;
            childProcessors = new SignalProcessor<?,?>[split.getChannelCount()][];
            for (int channel=0; channel< split.getChannelCount(); channel++) {
                childProcessors[channel] = split.getConnectedProcessors(channel);
            }
        } else if (processor instanceof SignalPipeline) {
            childProcessors = new SignalProcessor<?,?>[1][];
            childProcessors[0] = new SignalProcessor[]{((SignalPipeline) processor).getFirstProcessor()};
        } else {
            childProcessors = new SignalProcessor<?,?>[1][];
            childProcessors[0] = processor.getConnectedProcessors();
        }
        return childProcessors;
    }

    @Override
    public String toString() {
        return "SignalPump{" +
                "signalSource=" + signalSource +
                ", rootProcessors=" + rootProcessors +
                '}';
    }

    private static SignalProcessor<?, ?> decomposePipeline(final SignalProcessor<?, ?> processor) {
        return processor instanceof SignalPipeline
                ? decomposePipeline(((SignalPipeline) processor).getFirstProcessor())
                : processor;
    }

    private static class SignalProcessorPath {

        private List<SignalProcessor<?,?>> path = new ArrayList<SignalProcessor<?,?>>();

        /**
         * Creates lists from the provided tree. Any {@link SignalPipeline}s are removed in this process.
         *
         * @param processor tree
         * @return lists describing all straight paths through the tree
         */
        public static SignalProcessorPath[] create(final SignalProcessor<?,?> processor) {
            final List<SignalProcessorPath> signalProcessorPaths = new ArrayList<SignalProcessorPath>();
            final SignalProcessorPath signalProcessorPath = new SignalProcessorPath();
            signalProcessorPath.add(processor);
            signalProcessorPaths.add(signalProcessorPath);
            create(processor, signalProcessorPaths, signalProcessorPath);
            return signalProcessorPaths.toArray(new SignalProcessorPath[signalProcessorPaths.size()]);
        }

        private static void create(final SignalProcessor<?, ?> p, final List<SignalProcessorPath> paths, final SignalProcessorPath path) {
            //final SignalProcessor<?, ?> processor = decomposePipeline(p);
            // is it ok to not take multi channel kids into account? - yes, because we don't optimize them at all! (hs)
            final SignalProcessor<?, ?>[] kids = p.getConnectedProcessors();
            if (kids.length >= 1) {
                for (int i=0; i<kids.length; i++) {
                    final SignalProcessorPath kidPath;
                    if (i == kids.length-1) {
                        kidPath = path;
                    } else {
                        kidPath = path.clone();
                        paths.add(kidPath);
                    }
                    kidPath.add(kids[i]);
                    create(kids[i], paths, kidPath);
                }
            }
        }

        public void add(final SignalProcessor<?,?> processor) {
            //path.add(decomposePipeline(processor));
            path.add(processor);
        }

        public SignalProcessor<?,?>[] path() {
            return path.toArray(new SignalProcessor[path.size()]);
        }

        public SignalProcessorPath clone() {
            final SignalProcessorPath signalProcessorPath = new SignalProcessorPath();
            signalProcessorPath.path.addAll(this.path);
            return signalProcessorPath;
        }
    }
}
