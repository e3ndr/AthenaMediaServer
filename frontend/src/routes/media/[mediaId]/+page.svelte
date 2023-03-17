<script>
	import AspectPoster from '$lib/aspect-ratio/AspectPoster.svelte';
	import PageTitle from '$lib/PageTitle.svelte';

	import { VIDEO_QUALITIES } from '$lib/constants.mjs';

	/** @type {import('./$types').PageData} */
	export let data;

	let selectedVideoStream = 0;
	let selectedAudioStream = 0;
	let selectedVideoQuality = 'SOURCE';

	// Figure out the default video and video streams.
	for (const videoStream of data.media.files.streams.video) {
		if (data.media.files.streams.defaultStreams.includes(videoStream.id)) {
			selectedVideoStream = videoStream;
			break;
		}
	}
	for (const audioStream of data.media.files.streams.audio) {
		if (data.media.files.streams.defaultStreams.includes(audioStream.id)) {
			selectedAudioStream = audioStream;
			break;
		}
	}
</script>

<PageTitle title={[`${data.media.info.title} (${data.media.info.year})`]} />

<div class="flex flex-col sm:flex-row">
	<div class="flex-0 w-48 mx-auto">
		<AspectPoster>
			<img
				class="w-full h-full object-cover rounded"
				alt=""
				src={data.media.files.images.posterUrl}
			/>
		</AspectPoster>
	</div>

	<div class="mx-6 mt-4 flex-1">
		<h1 class="text-4xl font-bold">{data.media.info.title}</h1>
		<h2>
			<span class="text-sm mr-3">{data.media.info.year}</span>
			<span class="ml-1 text-xs">{data.media.info.genres.join(', ')}</span>
		</h2>

		<ul class="mt-2 space-x-2">
			{#each [[null, data.media.info.rating], ...Object.entries(data.media.info.ratings)] as [critic, rating]}
				<li class="inline-block rounded px-1.5 py-0.5 bg-base-1 text-base-12 text-sm">
					{#if critic}
						<icon
							class="inline-block translate-y-0.5 h-4 w-fit"
							data-icon="critic/{critic.toLowerCase()}"
						/>
					{/if}
					<span>
						{rating}
					</span>
				</li>
			{/each}
		</ul>

		<div class="mt-8">
			<a
				href="/media/{data.media
					.id}/player?quality={selectedVideoQuality}&streams={selectedVideoStream.id},{selectedAudioStream.id}"
				class="bg-primary-8 px-2 py-1 rounded-md"
			>
				<icon class="inline-block translate-y-0.5 h-4 w-4" data-icon="icon/play" />
				Watch
			</a>
		</div>

		<p class="mt-6">
			{data.media.info.summary}
		</p>

		<table class="-mx-4 mt-8 w-50 font-medium text-sm">
			<tr>
				<td class="px-4 text-base-11">Directors:</td>
				<td>
					{data.media.info.people.directors.join(', ')}
				</td>

				<td class="px-4 text-base-11">Studios:</td>
				<td>
					{data.media.info.studios.join(', ')}
				</td>
			</tr>
			<tr>
				<td class="px-4 text-base-11">Writers:</td>
				<td>
					{data.media.info.people.writers.join(', ')}
				</td>

				<td class="px-4 text-base-11">Video:</td>
				<td>
					{data.media.files.streams.video.map((s) => s.name || s.codec).join(', ')}
				</td>
			</tr>
			<tr>
				<td class="px-4 text-base-11">Starring:</td>
				<td>
					{data.media.info.people.actors.join(', ')}
				</td>

				<td class="px-4 text-base-11">Audio:</td>
				<td>
					{data.media.files.streams.audio.map((s) => s.name || s.codec).join(', ')}
				</td>
			</tr>
		</table>
	</div>
</div>
