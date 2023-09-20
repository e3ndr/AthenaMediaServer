<script>
	import { onMount } from 'svelte';

	/** @type {import('./$types').PageData} */
	export let data;

	const QUALITY_STRINGS = {
		SOURCE: 'Original Quality',
		UHD: 'Ultra-High Definition (4K)',
		FHD: 'Full High Definition (1080p)',
		HD: 'High Definition (720p)',
		SD: 'Standard Definition (480p)',
		LD: 'Low Definition (240p)'
	};

	onMount(() => {
		console.log(data);
	});
</script>

<!-- <pre>
{JSON.stringify(data, null, 2)}
</pre> -->

<svelte:head>
	<script>
		var showingAdvancedOptions = false;

		function toggleAdvancedOptions() {
			try {
				var settingsForm = document.getElementById('settings-form');

				// Some older browsers only support `classList.add()` and not `classList = ...`
				// Conversely, some only support the latter way of doing things...

				if (!showingAdvancedOptions) {
					showingAdvancedOptions = true;
					if (typeof settingsForm.classList.add == 'undefined') {
						settingsForm.classList = 'show-advanced';
					} else {
						settingsForm.classList.add('show-advanced');
					}
				} else {
					showingAdvancedOptions = false;
					if (typeof settingsForm.classList.add == 'undefined') {
						settingsForm.classList = '';
					} else {
						settingsForm.classList.remove('show-advanced');
					}
				}
			} catch (e) {
				alert('An error occurred:\n\n' + e);
			}
		}

		window.onload = function () {};
	</script>
</svelte:head>

<a href="/">Go Back</a>

<h1 style="margin: 0;">Servers:</h1>
{#each data.settings.servers as server, idx}
	<form method="POST" action="/settings/save/delete" style="margin-bottom: 4px;">
		<input name="arr-idx" type="hidden" value={idx} />
		<input type="input" value={server} disabled style="width: 42ch;" />
		<input type="submit" value="Delete" style="width: 8ch;" />
	</form>
{/each}
{#if data.settings.servers.length < 3}
	<form method="POST" action="/settings/save/add">
		<input name="server" type="input" placeholder="https://example.com:8125" style="width: 42ch;" />
		<input type="submit" value="Add" style="width: 8ch;" />
	</form>
{/if}

<br />
<br />
<br />

<h1 style="margin: 0;">Settings:</h1>
<form id="settings-form" method="POST" action="/settings/save">
	<table>
		<colgroup>
			<col span="1" style="width: 175px;" />
			<col span="1" style="width: auto;" />
		</colgroup>
		<tbody>
			<tr>
				<td>
					<label for="media-quality">Media Quality</label>
				</td>
				<td>
					<select
						name="media-quality"
						id="media-quality"
						disabled={data.availableSettings.qualities.length == 1}
					>
						{#each data.availableSettings.qualities as quality}
							{@const isSelected = data.settings.preferredQuality == quality}
							<option value={quality} selected={isSelected}>{QUALITY_STRINGS[quality]}</option>
						{/each}
					</select>
				</td>
			</tr>

			<tr class="advanced-option">
				<td>
					<label for="container-format">Container Format</label>
				</td>
				<td>
					<select
						name="container-format"
						id="container-format"
						disabled={data.availableSettings.containers.length == 1}
					>
						{#each data.availableSettings.containers as container}
							{@const isSelected = data.settings.deliveryPreferences.c == container}
							<option value={container} selected={isSelected}>{container}</option>
						{/each}
					</select>
				</td>
			</tr>

			<tr class="advanced-option">
				<td>
					<label for="video-codec">Video Codec</label>
				</td>
				<td>
					<select
						name="video-codec"
						id="video-codec"
						disabled={data.availableSettings.videoCodecs.length == 1}
					>
						{#each data.availableSettings.videoCodecs as codec}
							{@const isSelected = data.settings.deliveryPreferences.v == codec}
							<option value={codec} selected={isSelected}>{codec}</option>
						{/each}
					</select>
				</td>
			</tr>

			<tr class="advanced-option">
				<td>
					<label for="audio-codec">Audio Codec</label>
				</td>
				<td>
					<select
						name="audio-codec"
						id="audio-codec"
						disabled={data.availableSettings.audioCodecs.length == 1}
					>
						{#each data.availableSettings.audioCodecs as codec}
							{@const isSelected = data.settings.deliveryPreferences.a == codec}
							<option value={codec} selected={isSelected}>{codec}</option>
						{/each}
					</select>
				</td>
			</tr>
		</tbody>
	</table>

	<br />

	<input type="submit" value="Save" />

	<button id="advanced-options-toggle" onclick="toggleAdvancedOptions(); return false;">
		Toggle advanced options
	</button>
</form>

<style>
	select {
		width: 100%;
	}

	:global(.hidden) {
		display: none;
	}

	.advanced-option {
		display: none;
	}

	:global(.show-advanced) .advanced-option {
		display: table-row !important;
	}
</style>
