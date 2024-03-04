import puppeteer from 'puppeteer';
import minimist from 'minimist';
import fs from 'fs-extra';

const run = async (
	name,
	countryCode = 'US',
	stateCode = '',
	minPrice,
	maxPrice,
	noPriceIncluded,
	minRatings,
	maxRatings,
	minAverage,
	maxAverage,
) => {
	// set country and state
	const setShipTo = async (countryCode, stateCode) => {
		return await page.evaluate(
			async (countryCode, stateCode) => {
				const fetchResult = await fetch('https://www.vivino.com/api/ship_to/', {
					headers: {
						'content-type': 'application/json',
						'x-csrf-token': document.querySelector('[name="csrf-token"]').content,
					},
					body: JSON.stringify({
						country_code: countryCode,
						state_code: stateCode,
					}),
					method: 'PUT',
				});
				if (fetchResult.status === 200) {
					const result = await fetchResult.json();
					if (
						result.ship_to.country_code.toLowerCase() === countryCode.toLowerCase() &&
						result.ship_to.state_code.toLowerCase() === stateCode.toLowerCase()
					) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			},
			countryCode,
			stateCode,
		);
	};

	// check country and state
	const isShipTo = async (countryCode, stateCode) => {
		return await page.evaluate(
			(countryCode, stateCode) => {
				if (
					countryCode.toLowerCase() === window.__PRELOADED_COUNTRY_CODE__.toLowerCase() &&
					stateCode.toLowerCase() === window.__PRELOADED_STATE_CODE__.toLowerCase()
				) {
					return true;
				}
				return false;
			},
			countryCode,
			stateCode,
		);
	};

	// Set default state for the US
	if (countryCode.toLowerCase() === 'us' && stateCode === '') {
		stateCode = 'CA';
	}

	const BASE_URL = 'https://www.vivino.com';
	const SEARCH_PATH = '/search/wines?q=';
	const STATUS_FULL = 'FULL_DATA';
	const STATUS_ERROR_RESPONSE = 'RESPONSE_ERROR';
	const STATUS_ERROR_SHIP_TO = 'SHIP_TO_ERROR';
	const STATUS_ERROR_SHIP_TO_CONFIRM = 'SHIP_TO_CONFIRM_ERROR';
	const STATUS_ERROR_EXCEPTION = 'SOME_EXCEPTION';
	const PAUSE_MULTIPLIER = 15;

	const result = { vinos: [] };

	const browserURL = 'http://127.0.0.1:9222';
	let browser = null;

	browser = await puppeteer.launch({
		headless: true,
		defaultViewport: { width: 1920, height: 1040 },
		devtools: false,
		args: ['--remote-debugging-port=9222', '--start-maximized'],
	});

	const page = await browser.newPage();

	// need to set User Agent else an empty result
	// it seems they detect headless Chrome
	await page.setUserAgent('Chrome/121.0.6167.184');

	try {
		page.setDefaultNavigationTimeout(0);

		//load home page
		await page.goto(BASE_URL); // , { waitUntil: 'networkidle2' }
		const resultSetShipTo = await setShipTo(countryCode, stateCode);

		//check the country and state
		let isDestinationRight = await isShipTo(countryCode, stateCode);
		if (!isDestinationRight) {
			// set country and state
			const resultSetShipTo = await setShipTo(countryCode, stateCode);
			if (resultSetShipTo) {
				await page.goto(BASE_URL, { waitUntil: 'networkidle2' });
				// check the country and state
				isDestinationRight = await isShipTo(countryCode, stateCode);
				if (!isDestinationRight) {
					console.log('"Ship To" changing can not be confirmed!');
					result.status = STATUS_ERROR_SHIP_TO_CONFIRM;
					return;
				}
			} else {
				console.log('"Ship To" was not changed!');
				result.status = STATUS_ERROR_SHIP_TO;
				return;
			}
		}
	} catch (error) {
		result.status = STATUS_ERROR_EXCEPTION;
		result.message = error;
		console.log('Browser Exception:', error);
	} finally {
		//console.log('Finish!');
		// output results to the file
		//console.log(JSON.stringify(result.vinos, null, 2)); // 자바로 리턴됨
		// const outFile = fs.createWriteStream('vivino-out.json');
		// outFile.write(JSON.stringify(result, null, 2));
		// outFile.end();
		//await page.close();
		//browser.disconnect();
		// await browser.close();
		//
		// 브라우저는 계속 실행
		if (await isShipTo(countryCode, stateCode)) {
			console.log('브라우저 세팅 완료. ');
		} else {
			console.log('브라우저 세팅중 오류 발생. ');
		}
	}
};

const args = minimist(process.argv.slice(2));
//console.log(args);
console.log('브라우저 세팅 시작. ');
const {
	name,
	country,
	state,
	minPrice,
	maxPrice,
	noPriceIncluded,
	minRatings,
	maxRatings,
	minAverage,
	maxAverage,
} = args;

run(
	name,
	country,
	state,
	minPrice,
	maxPrice,
	noPriceIncluded,
	minRatings,
	maxRatings,
	minAverage,
	maxAverage,
);
