package com.philsoft.metrotripper.database.contracts;

/**
 * Created by polson on 1/19/15.
 */
public class StopContract {

	public static final String TABLE_NAME = "t_stops";
	public static final String[] ALL_COLUMNS;

	static {
		// @formatter:off
		ALL_COLUMNS = new String[] {
				StopContract.STOP_ID,
				StopContract.STOP_NAME,
				StopContract.STOP_DESC,
				StopContract.STOP_LAT,
				StopContract.STOP_LON,
				StopContract.STOP_STREET,
				StopContract.STOP_CITY,
				StopContract.STOP_REGION,
				StopContract.STOP_POSTCODE,
				StopContract.STOP_COUNTRY,
				StopContract.ZONE_ID,
				StopContract.WHEELCHAIR_BOARDING,
				StopContract.STOP_URL
		};
		// @formatter:on
	}

	/**
	 * None
	 * <p/>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String STOP_ID = "stop_id";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(49)
	 * </P>
	 */
	public static final String STOP_NAME = "stop_name";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(13)
	 * </P>
	 */
	public static final String STOP_DESC = "stop_desc";

	/**
	 * None
	 * <p/>
	 * Type: NUMERIC(11,6)
	 * </P>
	 */
	public static final String STOP_LAT = "stop_lat";

	/**
	 * None
	 * <p/>
	 * Type: NUMERIC(11,6)
	 * </P>
	 */
	public static final String STOP_LON = "stop_lon";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(35)
	 * </P>
	 */
	public static final String STOP_STREET = "stop_street";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(19)
	 * </P>
	 */
	public static final String STOP_CITY = "stop_city";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(1)
	 * </P>
	 */
	public static final String STOP_REGION = "stop_region";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(1)
	 * </P>
	 */
	public static final String STOP_POSTCODE = "stop_postcode";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(1)
	 * </P>
	 */
	public static final String STOP_COUNTRY = "stop_country";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(1)
	 * </P>
	 */
	public static final String ZONE_ID = "zone_id";

	/**
	 * None
	 * <p/>
	 * Type: INTEGER(1)
	 * </P>
	 */
	public static final String WHEELCHAIR_BOARDING = "wheelchair_boarding";

	/**
	 * None
	 * <p/>
	 * Type: VARCHAR(62)
	 * </P>
	 */
	public static final String STOP_URL = "stop_url";


	// Prevent instantiation of this class
	private StopContract() {
	}
}
