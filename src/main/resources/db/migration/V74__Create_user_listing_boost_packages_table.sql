CREATE TABLE user_listing_boost_packages (
    user_listing_boost_package_id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    boost_package_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    remaining_featured_quota INT,
    remaining_hot_badge_quota INT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (boost_package_id) REFERENCES boost_packages(boost_package_id)
);

CREATE INDEX idx_user_boost_pkg_user ON user_listing_boost_packages(user_id);
CREATE INDEX idx_user_boost_pkg_boost_pkg ON user_listing_boost_packages(boost_package_id);
CREATE INDEX idx_user_boost_pkg_status ON user_listing_boost_packages(status);
CREATE INDEX idx_user_boost_pkg_user_status ON user_listing_boost_packages(user_id, status);
