package com.condation.cms.modules.git;

/*-
 * #%L
 * backup-module
 * %%
 * Copyright (C) 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.RepoCheckoutEvent;
import com.condation.cms.api.extensions.server.ServerLifecycleExtensionPoint;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.scheduler.CronJobScheduler;
import com.condation.modules.api.annotation.Extension;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(ServerLifecycleExtensionPoint.class)
public class GitLifecycleExtension extends ServerLifecycleExtensionPoint {

	public static RepositoryManager REPO_MANAGER;

	@Override
	public void stopped() {

	}

	@Override
	public void started() {
		try {
			var injector = getContext().get(InjectorFeature.class).injector();
			var scheduler = injector.getInstance(Key.get(CronJobScheduler.class, Names.named("server")));
			
			REPO_MANAGER = new RepositoryManager(scheduler);
			
			REPO_MANAGER.init(Path.of("config/git.yaml"));
			
			
			injector.getInstance(Key.get(EventBus.class, Names.named("server"))).register(RepoCheckoutEvent.class, (event) -> {
				REPO_MANAGER.updateRepo(event.repo());
			});
		} catch (Exception ex) {
			log.error("error starting git module", ex);
		}
	}
}
