<script>
	import PageTitle from '$lib/PageTitle.svelte';

	/** @type {import('./$types').PageData} */
	export let data;
</script>

<PageTitle title={[`${data.media.info.title} (${data.media.info.year})`]} />

<table>
	<tr>
		<td>
			<img
				style="width: 192px; height: 307px; object-fit: cover; border-radius: 5px; display: inline-block;"
				alt=""
				src={data.media.files.images.posterUrl}
			/>
		</td>
		<td style="vertical-align: top; padding-left: 15px;">
			<div style="display: inline-block;">
				<div>
					<h1 style="margin: 0px;">{data.media.info.title} ({data.media.info.year})</h1>
					<h2 style="margin: 0px; font-size: large;">
						{data.media.info.genres.join(', ')}
					</h2>
				</div>

				<p>
					{data.media.info.summary}
				</p>

				<div style="margin-top: 20px; margin-bottom: 20px;">
					<a href="/media/{data.media.id}/player"> Watch </a>
				</div>

				<table>
					<tr>
						<td>Studios:</td>
						<td>
							{data.media.info.studios.map((s) => s.name).join(', ')}
						</td>

						<td>Directors:</td>
						<td>
							{data.media.info.directors.map((p) => p.name).join(', ')}
						</td>
					</tr>
					<tr />
					<tr>
						<td>Starring:</td>
						<td>
							{data.media.info.actors.map((p) => p.name).join(', ')}
						</td>
						<td>Codecs:</td>
						<td>
							{data.media.files.streams.video
								.map((s) => s.name || s.codec.toUpperCase())
								.join(', ')}
							<br />
							{data.media.files.streams.audio
								.map((s) => s.name || s.codec.toUpperCase())
								.join(', ')}
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
</table>
