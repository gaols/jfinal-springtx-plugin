package com.github.gaols.plugins;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Field;

public class SpringTxAwareActiveRecordPlugin extends ActiveRecordPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SpringTxAwareActiveRecordPlugin.class);

    public SpringTxAwareActiveRecordPlugin(String configName, DataSource dataSource, int transactionLevel) {
        this(new SpringTxAwareConfig(configName, wrapDataSource(dataSource), transactionLevel));
    }

    private static DataSource wrapDataSource(DataSource dataSource) {
        return new SpringTxAwareDataSource(dataSource);
    }

    public SpringTxAwareActiveRecordPlugin(DataSource dataSource) {
        this(DbKit.MAIN_CONFIG_NAME, dataSource);
    }

    public SpringTxAwareActiveRecordPlugin(String configName, DataSource dataSource) {
        this(configName, dataSource, DbKit.DEFAULT_TRANSACTION_LEVEL);
    }

    public SpringTxAwareActiveRecordPlugin(DataSource dataSource, int transactionLevel) {
        this(DbKit.MAIN_CONFIG_NAME, dataSource, transactionLevel);
    }

    public SpringTxAwareActiveRecordPlugin(Config config) {
        super(config);
        try {
            DataSource ds = config.getDataSource();
            Field field = ActiveRecordPlugin.class.getDeclaredField("dataSource");
            field.setAccessible(true);
            field.set(this, ds);
        } catch ( IllegalAccessException | NoSuchFieldException e) {
            logger.warn("no datasource field found for ActiveRecordPlugin");
        }
    }

    public SpringTxAwareActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider, int transactionLevel) {
        this(new SpringTxAwareConfig(configName, providerDataSource(dataSourceProvider), transactionLevel));
    }

    private static DataSource providerDataSource(IDataSourceProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("IDataSourceProvider should not be null");
        }
        return wrapDataSource(provider.getDataSource());
    }

    public SpringTxAwareActiveRecordPlugin(IDataSourceProvider dataSourceProvider) {
        this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider);
    }

    public SpringTxAwareActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider) {
        this(configName, dataSourceProvider, 4);
    }

    public SpringTxAwareActiveRecordPlugin(IDataSourceProvider dataSourceProvider, int transactionLevel) {
        this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider, transactionLevel);
    }

    private Config getDbConfig() {
        try {
            Field field = ActiveRecordPlugin.class.getDeclaredField("config");
            field.setAccessible(true);
            return (Config) field.get(this);
        } catch ( IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("no datasource field found for ActiveRecordPlugin");
        }
    }

    @Override
    public ActiveRecordPlugin setShowSql(boolean showSql) {
        SpringTxAwareDataSource ds = (SpringTxAwareDataSource) getDbConfig().getDataSource();
        ds.setShowSql(showSql);
        super.setShowSql(false); // always disable JFinal's
        return this;
    }
}
