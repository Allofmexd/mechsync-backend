package com.mechsync.modules.technicians.application.port.in;

import com.mechsync.modules.technicians.application.dto.UpdateTechnicianCommand;
import com.mechsync.modules.technicians.domain.model.Technician;

public interface UpdateTechnicianUseCase {

    Technician update(UpdateTechnicianCommand command);
}
