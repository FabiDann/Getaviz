package org.getaviz.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class Generator_DB {
    private Log log = LogFactory.getLog(this.getClass());
    private Metaphor metaphor;

    Generator_DB(SettingsConfiguration config) {
        metaphor = MetaphorFactory.createMetaphor(config);
    }

    void run() {
        log.info("Generator started");
        metaphor.generate();
        log.info("Generator finished");
    }
}
