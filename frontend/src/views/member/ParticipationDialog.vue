<template>
  <v-dialog v-model="dialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="headline"> Select Participant </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Rating"
                :rules="[
                  (v) =>
                    !v ||
                    isNumberValid(v) ||
                    'Rating between 1 and 5 is required',
                ]"
                required
                v-model="participation.rating"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-participation-dialog')"
        >
          Close
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="makeParticipant">
          Make Participant
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Vue, Component, Prop, Model } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Participation from '@/models/participation/Participation';
import Enrollment from '@/models/enrollment/Enrollment';
import Activity from '@/models/activity/Activity';
import * as Console from 'console';

@Component({})
export default class ParticipationDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Enrollment, required: true }) readonly enrollment!: Enrollment;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  participation: Participation = new Participation();

  async created() {
    this.participation.activityId = this.activity.id;
    this.participation.volunteerId = this.enrollment.volunteerId;
  }

  isNumberValid(value: any) {
    if (!/^\d+$/.test(value)) return false;
    const parsedValue = parseInt(value);
    return parsedValue >= 1 && parsedValue <= 5;
  }

  async makeParticipant() {
    if ((this.$refs.form as Vue & { validate: () => boolean }).validate()) {
      if (this.activity.id !== null) {
        try {
          await RemoteServices.createParticipation(
            this.$store.getters.getUser.id,
            this.activity.id,
            this.participation,
          );
          this.$emit('save-participation', this.enrollment);
        } catch (error) {
          await this.$store.dispatch('error', error);
        }
      }
    }
  }
}
</script>

<style scoped lang="scss"></style>
