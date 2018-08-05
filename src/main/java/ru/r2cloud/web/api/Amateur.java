package ru.r2cloud.web.api;

import com.eclipsesource.json.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import ru.r2cloud.model.SatelliteType;
import ru.r2cloud.satellite.ObservationResultDao;
import ru.r2cloud.satellite.SatelliteDao;
import ru.r2cloud.satellite.Scheduler;
import ru.r2cloud.util.Configuration;
import ru.r2cloud.util.JsonUtil;
import ru.r2cloud.web.AbstractHttpController;
import ru.r2cloud.web.ModelAndView;

public class Amateur extends AbstractHttpController {

	private final Configuration config;
	private final SatelliteDao dao;
	private final ObservationResultDao resultDao;
	private final Scheduler scheduler;

	public Amateur(Configuration config, SatelliteDao dao, Scheduler scheduler, ObservationResultDao resultDao) {
		this.config = config;
		this.dao = dao;
		this.resultDao = resultDao;
		this.scheduler = scheduler;
	}

	@Override
	public ModelAndView doGet(IHTTPSession session) {
		ModelAndView result = new ModelAndView();
		JsonObject entity = new JsonObject();
		boolean isEnabled = config.getBoolean("satellites.enabled");
		entity.add("enabled", isEnabled);
		if (isEnabled) {
			entity = JsonUtil.serialize(scheduler, resultDao, dao.findAll(SatelliteType.AMATEUR));
		}
		result.setData(entity.toString());
		return result;
	}

	@Override
	public String getRequestMappingURL() {
		return "/api/v1/admin/amateur";
	}

}
