package io.flutter.vmService.frame;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import io.flutter.vmService.DartVmServiceDebugProcess;
import io.flutter.vmService.IsolatesInfo;
import org.dartlang.vm.service.element.Frame;
import org.dartlang.vm.service.element.InstanceRef;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DartVmServiceSuspendContext extends XSuspendContext {
  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final DartVmServiceExecutionStack myActiveExecutionStack;

  private List<XExecutionStack> myExecutionStacks;
  private final boolean myAtAsyncSuspension;

  public DartVmServiceSuspendContext(@NotNull final DartVmServiceDebugProcess debugProcess,
                                     @NotNull final IsolateRef isolateRef,
                                     @NotNull final Frame topFrame,
                                     @Nullable final InstanceRef exception,
                                     boolean atAsyncSuspension) {
    myDebugProcess = debugProcess;
    myActiveExecutionStack = new DartVmServiceExecutionStack(debugProcess, isolateRef.getId(), isolateRef.getName(), topFrame, exception);
    myAtAsyncSuspension = atAsyncSuspension;
  }

  @NotNull
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myActiveExecutionStack;
  }

  public boolean getAtAsyncSuspension() {
    return myAtAsyncSuspension;
  }

  @Override
  public void computeExecutionStacks(@NotNull final XExecutionStackContainer container) {
    if (myExecutionStacks == null) {
      final Collection<IsolatesInfo.IsolateInfo> isolateInfos = myDebugProcess.getIsolateInfos();
      myExecutionStacks = new ArrayList<>(isolateInfos.size());
      for (IsolatesInfo.IsolateInfo isolateInfo : isolateInfos) {
        if (Objects.equals(isolateInfo.getIsolateId(), myActiveExecutionStack.getIsolateId())) {
          myExecutionStacks.add(myActiveExecutionStack);
        }
        else {
          myExecutionStacks
            .add(new DartVmServiceExecutionStack(myDebugProcess, isolateInfo.getIsolateId(), isolateInfo.getIsolateName(), null, null));
        }
      }
    }

    container.addExecutionStack(myExecutionStacks, true);
  }
}
