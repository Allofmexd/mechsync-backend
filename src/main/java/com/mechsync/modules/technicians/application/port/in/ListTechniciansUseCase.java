package com.mechsync.modules.technicians.application.port.in;

import com.mechsync.modules.technicians.domain.model.Technician;
import java.util.List;

public interface ListTechniciansUseCase {

    List<Technician> list();
}
