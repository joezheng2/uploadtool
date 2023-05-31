/*
 * Copyright (c) 2007, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @key stress randomness
 *
 * @summary converted from VM Testbase gc/gctests/WeakReference/weak007.
 * VM Testbase keywords: [gc, stress, stressopt, nonconcurrent]
 *
 * @library /vmTestbase
 *          /test/lib
 * @build jdk.test.whitebox.WhiteBox
 * @run driver jdk.test.lib.helpers.ClassFileInstaller jdk.test.whitebox.WhiteBox
 * @run main/othervm -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI gc.gctests.WeakReference.weak007.weak007 -t 1
 */

package gc.gctests.WeakReference.weak007;

import jdk.test.whitebox.WhiteBox;
import nsk.share.TestFailure;
import nsk.share.gc.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

/**
 * Test that GC correctly clears weak references.
 *
 * This test creates a number of weak references,
 * each of which points to the next, then provokes
 * GC with WB.fullGC(). The test succeeds
 * if last reference has been cleared and the object
 * has been finalized.
 */
public class weak007 extends ThreadedGCTest {

    class Worker implements Runnable {

        private int length = 10000;
        private int objectSize = 10000;
        private Reference[] references;

        private void makeReferences() {
            references[length - 1] = null;
            FinMemoryObject obj = new FinMemoryObject(objectSize);
            references[0] = new WeakReference(obj);
            for (int i = 1; i < length; ++i) {
                references[i] = new WeakReference(references[i - 1]);
            }
            for (int i = 0; i < length - 1; ++i) {
                references[i] = null;
            }
        }

        public void run() {
            makeReferences();
            WhiteBox.getWhiteBox().fullGC();
            if (getExecutionController().continueExecution()) {
                if (references[length - 1].get() != null) {
                    throw new TestFailure("Last weak reference has not been cleared");
                }
            } else {
                log.info("Completed iterations: " + getExecutionController().getIteration());
            }
        }

        public Worker() {
            log.info("Array size: " + length);
            log.info("Object size: " + objectSize);
            references = new WeakReference[length];
        }
    }

    @Override
    protected Runnable createRunnable(int i) {
        return new Worker();
    }

    public static void main(String[] args) {
        GC.runTest(new weak007(), args);
    }
}
